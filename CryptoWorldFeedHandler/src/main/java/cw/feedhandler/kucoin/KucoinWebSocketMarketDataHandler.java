package cw.feedhandler.kucoin;

import com.google.gson.Gson;
import cw.common.db.mysql.Exchange;
import cw.common.db.mysql.TradingPair;
import cw.common.json.FlyweightStringBuilder;
import cw.common.json.JsonParser;
import cw.common.md.ChronicleUtil;
import cw.feedhandler.AbstractWebSocketMarketDataHandler;
import cw.feedhandler.kucoin.gson.Response;
import cwp.db.dynamodb.DynamoDbUtil;
import org.apache.logging.log4j.LogManager;
import org.eclipse.collections.impl.map.mutable.primitive.ObjectLongHashMap;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class KucoinWebSocketMarketDataHandler extends AbstractWebSocketMarketDataHandler {
    private static final String SUBSCRIBE_PREFIX = "{\"type\": \"subscribe\",\"topic\": \"/market/ticker:";
    private static final String SUBSCRIBE_SUFFIX_PRE_ID = "\",\"privateChannel\": false,\"response\": true, \"id\":";
    private static final String SUBSCRIBE_SUFFIX_POST_ID = "}";
    private static final StringBuilder SUBSCRIBE_STRING_BUILDER = new StringBuilder(SUBSCRIBE_PREFIX);

    private static int REQUEST_ID = 1;

    private final ObjectLongHashMap<String> marketToTime;
    private final KucoinJsonParserListener responseListener;
    private final JsonParser jsonParser;

    public KucoinWebSocketMarketDataHandler() throws Exception {
        super();

        this.logger = LogManager.getLogger(KucoinWebSocketMarketDataHandler.class.getSimpleName());
        this.uri = new URI(getWebSocketEndpoint());
        this.quoteTopicToTradingPair = generateTopicToTradingPair(DynamoDbUtil.getMarketDataQuoteTopics(getExchange().getExchangeName()));
        this.quoteMap = ChronicleUtil.getQuoteMap(getExchange(), TradingPair.BTCUSDT);
        this.candlestickMaps = new HashMap<>();

        this.marketToTime = new ObjectLongHashMap<>();
        this.responseListener = new KucoinJsonParserListener();
        this.jsonParser = new JsonParser(new FlyweightStringBuilder());
        this.jsonParser.setListener(this.responseListener);

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
        for (String topic : this.quoteTopicToTradingPair.keySet()) {
            SUBSCRIBE_STRING_BUILDER.append(topic);
            SUBSCRIBE_STRING_BUILDER.append(SUBSCRIBE_SUFFIX_PRE_ID);
            SUBSCRIBE_STRING_BUILDER.append(REQUEST_ID++);
            SUBSCRIBE_STRING_BUILDER.append(SUBSCRIBE_SUFFIX_POST_ID);

            String request = SUBSCRIBE_STRING_BUILDER.toString();
            this.webSocketClient.send(request);
            SUBSCRIBE_STRING_BUILDER.setLength(SUBSCRIBE_PREFIX.length());

            this.logger.info("Subscribed to {}.", request);

            throttleRequest();
        }
    }

    @Override
    protected void processMessage(String message) {
        this.jsonParser.parse(message);
        this.jsonParser.eoj();

        String market = this.responseListener.market;
        long time = this.marketToTime.get(market);

        if (this.responseListener.time > time) {
            this.marketToTime.put(market, this.responseListener.time);

            TradingPair tradingPair = this.quoteTopicToTradingPair.get(market.substring(market.indexOf(":") + 1));
            if (tradingPair == null) return;

            this.quote.setTradingPair(tradingPair);
            this.quote.setBidPrice(this.responseListener.bid);
            this.quote.setBidSize(this.responseListener.bidSize);
            this.quote.setAskPrice(this.responseListener.ask);
            this.quote.setAskSize(this.responseListener.askSize);

            this.quoteMap.put(tradingPair, this.quote);
        }
    }
}
