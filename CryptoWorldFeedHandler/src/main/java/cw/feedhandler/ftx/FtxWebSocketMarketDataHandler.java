package cw.feedhandler.ftx;

import cw.common.json.JsonParser;
import cw.common.db.mysql.Exchange;
import cw.common.db.mysql.TradingPair;
import cw.feedhandler.AbstractWebSocketMarketDataHandler;
import cw.common.env.EnvUtil;
import cw.common.json.FlyweightStringBuilder;
import cw.common.md.Quote;
import cwp.db.dynamodb.DynamoDbUtil;
import net.openhft.chronicle.map.ChronicleMapBuilder;
import org.apache.logging.log4j.LogManager;
import org.eclipse.collections.impl.map.mutable.primitive.ObjectLongHashMap;

import java.io.File;
import java.net.URI;
import java.util.Map;

public class FtxWebSocketMarketDataHandler extends AbstractWebSocketMarketDataHandler {
    private static final String SUBSCRIBE_PREFIX = "{\"op\": \"subscribe\", \"channel\": \"ticker\", \"market\": \"";
    private static final String SUBSCRIBE_SUFFIX = "\", \"id\": 1}";
    private static final StringBuilder SUBSCRIBE_STRING_BUILDER = new StringBuilder(SUBSCRIBE_PREFIX);

    private final ObjectLongHashMap<String> marketToTime;
    private final FtxQuoteJsonParserListener quoteListener;
    private final JsonParser jsonParser;

    public FtxWebSocketMarketDataHandler() throws Exception {
        super();

        this.logger = LogManager.getLogger(FtxWebSocketMarketDataHandler.class.getSimpleName());
        this.uri = new URI(getWebSocketEndpoint());
        this.topicToTradingPair = generateTopicToTradingPair(DynamoDbUtil.getMarketDataQuoteTopics(getExchange().getExchangeName()));
        String marketDataMap = DynamoDbUtil.getMarketDataQuoteMap(getExchange().getExchangeName(), EnvUtil.ENV.getEnvName());
        this.chronicleMap = ChronicleMapBuilder
                .of(TradingPair.class, Quote.class)
                .name(marketDataMap)
                .averageKey(TradingPair.ETHPERP)
                .entries(10)
                .createPersistedTo(new File(marketDataMap));

        this.marketToTime = new ObjectLongHashMap<>();
        this.quoteListener = new FtxQuoteJsonParserListener();
        this.jsonParser = new JsonParser(new FlyweightStringBuilder());
        this.jsonParser.setListener(this.quoteListener);

        validate();
    }

    @Override
    protected String getWebSocketEndpoint() {
        return "wss://ftx.com/ws/";
    }

    @Override
    protected Exchange getExchange() {
        return Exchange.FTX;
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

            TradingPair tradingPair = this.topicToTradingPair.get(market);
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
