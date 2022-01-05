package cw.feedhandler.binance;

import cw.common.env.EnvUtil;
import cw.common.json.FlyweightStringBuilder;
import cw.common.json.JsonParser;
import cw.common.md.Exchange;
import cw.common.md.Quote;
import cw.common.md.TradingPair;
import cw.feedhandler.AbstractWebSocketMarketDataHandler;
import cwp.db.dynamodb.DynamoDbUtil;
import net.openhft.chronicle.map.ChronicleMapBuilder;
import org.apache.logging.log4j.LogManager;
import org.eclipse.collections.impl.map.mutable.primitive.ObjectLongHashMap;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class BinanceWebSocketMarketDataHandler extends AbstractWebSocketMarketDataHandler {
    private static final String SUBSCRIBE_PREFIX = "{\"method\": \"SUBSCRIBE\", \"params\": [\"";
    private static final String SUBSCRIBE_SUFFIX = "\"], \"id\": 1}";
    private static final StringBuilder SUBSCRIBE_STRING_BUILDER = new StringBuilder(SUBSCRIBE_PREFIX);

    private final ObjectLongHashMap<String> streamToLastUpdateId;
    private final BinanceQuoteJsonParserListener quoteListener;
    private final JsonParser jsonParser;

    public BinanceWebSocketMarketDataHandler() throws Exception {
        super();

        this.logger = LogManager.getLogger(BinanceWebSocketMarketDataHandler.class.getSimpleName());
        this.uri = new URI(getWebSocketEndpoint());
        this.topicToTradingPair = generateTopicToTradingPair(DynamoDbUtil.getMarketDataTopics(getExchange().getExchangeName()));
        String marketDataMap = DynamoDbUtil.getMarketDataMap(getExchange().getExchangeName(), EnvUtil.ENV.getEnvName());
        this.chronicleMap = ChronicleMapBuilder
                .of(TradingPair.class, Quote.class)
                .name(marketDataMap)
                .averageKey(TradingPair.BTCUSDT)
                .entries(10)
                .createPersistedTo(new File(marketDataMap));

        this.streamToLastUpdateId = new ObjectLongHashMap<>();
        this.quoteListener = new BinanceQuoteJsonParserListener();
        this.jsonParser = new JsonParser(new FlyweightStringBuilder());
        this.jsonParser.setListener(this.quoteListener);

        validate();
    }

    @Override
    protected String getWebSocketEndpoint() {
        return "wss://stream.binance.com:9443/stream";
    }

    @Override
    protected Exchange getExchange() {
        return Exchange.BINANCE;
    }

    @Override
    protected Map<String, TradingPair> generateTopicToTradingPair(String[] topics) {
        Map<String, TradingPair> map = new HashMap<>();

        for (String topic : topics) {
            String symbol = topic.substring(0, topic.indexOf("@"));
            map.put(topic, TradingPair.EXCHANGE_TO_SYMBOL_TO_TRADING_PAIR.get(getExchange()).get(symbol));
        }

        return map;
    }

    @Override
    public void subscribe() {
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

        String stream = this.quoteListener.stream.toString();
        long lastUpdateId = this.streamToLastUpdateId.get(stream);

        if (this.quoteListener.lastUpdateId > lastUpdateId) {
            this.streamToLastUpdateId.put(stream, this.quoteListener.lastUpdateId);

            TradingPair tradingPair = this.topicToTradingPair.get(stream);
            if (tradingPair == null) return;

            this.quoteNativeReference.setTradingPair(tradingPair);
            this.quoteNativeReference.setBidPrice(this.quoteListener.bidPrice);
            this.quoteNativeReference.setBidSize(this.quoteListener.bidSize);
            this.quoteNativeReference.setAskPrice(this.quoteListener.askPrice);
            this.quoteNativeReference.setAskSize(this.quoteListener.askSize);

            this.chronicleMap.put(tradingPair, this.quoteNativeReference);
        }
    }
}
