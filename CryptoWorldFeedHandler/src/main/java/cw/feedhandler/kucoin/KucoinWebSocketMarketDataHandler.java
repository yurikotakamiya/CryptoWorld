package cw.feedhandler.kucoin;

import com.google.gson.Gson;
import cw.common.env.EnvUtil;
import cw.common.json.FlyweightStringBuilder;
import cw.common.json.JsonParser;
import cw.common.md.Exchange;
import cw.common.md.Quote;
import cw.common.md.TradingPair;
import cw.feedhandler.AbstractWebSocketMarketDataHandler;
import cw.feedhandler.kucoin.gson.Response;
import cwp.db.dynamodb.DynamoDbUtil;
import net.openhft.chronicle.map.ChronicleMapBuilder;
import org.apache.logging.log4j.LogManager;
import org.eclipse.collections.impl.map.mutable.primitive.ObjectLongHashMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.stream.Collectors;

public class KucoinWebSocketMarketDataHandler extends AbstractWebSocketMarketDataHandler {
    private static final String SUBSCRIBE_PREFIX = "{\"id\": 1, \"type\": \"subscribe\",\"topic\": \"/market/ticker:";
    private static final String SUBSCRIBE_SUFFIX = "\",\"privateChannel\": false,\"response\": true }";
    private static final StringBuilder SUBSCRIBE_STRING_BUILDER = new StringBuilder(SUBSCRIBE_PREFIX);

    private final ObjectLongHashMap<String> marketToTime;
    private final KucoinQuoteJsonParserListener quoteListener;
    private final JsonParser jsonParser;

    public KucoinWebSocketMarketDataHandler() throws Exception {
        super();

        this.logger = LogManager.getLogger(KucoinWebSocketMarketDataHandler.class.getSimpleName());
        this.uri = new URI(getWebSocketEndpoint());
        this.topicToTradingPair = generateTopicToTradingPair(DynamoDbUtil.getMarketDataTopics(getExchange().getExchangeName()));
        String marketDataMap = DynamoDbUtil.getMarketDataMap(getExchange().getExchangeName(), EnvUtil.ENV.getEnvName());
        this.chronicleMap = ChronicleMapBuilder
                .of(TradingPair.class, Quote.class)
                .name(marketDataMap)
                .averageKey(TradingPair.BTCUSDT)
                .entries(10)
                .createPersistedTo(new File(marketDataMap));

        this.marketToTime = new ObjectLongHashMap<>();
        this.quoteListener = new KucoinQuoteJsonParserListener();
        this.jsonParser = new JsonParser(new FlyweightStringBuilder());
        this.jsonParser.setListener(this.quoteListener);

        validate();
    }

    @Override
    protected String getWebSocketEndpoint() throws Exception {
        URL url = new URL("https://api.kucoin.com/api/v1/bullet-public");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");

        BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String input = reader.lines().collect(Collectors.joining());
        reader.close();

        Gson gson = new Gson();
        Response response = gson.fromJson(input, Response.class);

        return "wss://ws-api.kucoin.com/endpoint?token=" + response.data.token;
    }

    @Override
    protected Exchange getExchange() {
        return Exchange.KUCOIN;
    }

    @Override
    protected Map<String, TradingPair> generateTopicToTradingPair(String[] topics) {
        return TradingPair.EXCHANGE_TO_SYMBOL_TO_TRADING_PAIR.get(getExchange());
    }

    @Override
    protected void subscribe() {
        for (String topic : this.topicToTradingPair.keySet()) {
            SUBSCRIBE_STRING_BUILDER.append(topic);
            SUBSCRIBE_STRING_BUILDER.append(SUBSCRIBE_SUFFIX);

            String request = SUBSCRIBE_STRING_BUILDER.toString();
            this.webSocketClient.send(request);
            SUBSCRIBE_STRING_BUILDER.setLength(SUBSCRIBE_PREFIX.length());

            this.logger.info("Subscribed to {}.", request);
        }
    }

    @Override
    protected void processMessage(String message) {
        this.jsonParser.parse(message);
        this.jsonParser.eoj();

        String market = this.quoteListener.market;
        long time = this.marketToTime.get(market);

        if (this.quoteListener.time > time) {
            this.marketToTime.put(market, this.quoteListener.time);

            TradingPair tradingPair = this.topicToTradingPair.get(market.substring(market.indexOf(":") + 1));
            if (tradingPair == null) return;

            this.quoteNativeReference.setTradingPair(tradingPair);
            this.quoteNativeReference.setBidPrice(this.quoteListener.bid);
            this.quoteNativeReference.setBidSize(this.quoteListener.bidSize);
            this.quoteNativeReference.setAskPrice(this.quoteListener.ask);
            this.quoteNativeReference.setAskSize(this.quoteListener.askSize);

            this.chronicleMap.put(tradingPair, this.quoteNativeReference);
        }
    }
}
