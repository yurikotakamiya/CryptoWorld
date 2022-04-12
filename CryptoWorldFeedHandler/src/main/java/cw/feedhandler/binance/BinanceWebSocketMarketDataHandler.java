package cw.feedhandler.binance;

import cw.common.db.mysql.CandlestickInterval;
import cw.common.db.mysql.Exchange;
import cw.common.db.mysql.TradingPair;
import cw.common.json.FlyweightStringBuilder;
import cw.common.json.JsonParser;
import cw.common.md.ChronicleUtil;
import cw.feedhandler.AbstractWebSocketMarketDataHandler;
import cwp.db.dynamodb.DynamoDbUtil;
import org.apache.logging.log4j.LogManager;
import org.eclipse.collections.impl.map.mutable.primitive.ObjectLongHashMap;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class BinanceWebSocketMarketDataHandler extends AbstractWebSocketMarketDataHandler {
    private static final String SUBSCRIBE_PREFIX = "{\"method\": \"SUBSCRIBE\", \"params\": [\"";
    private static final String SUBSCRIBE_SUFFIX_PRE_ID = "\"], \"id\":";
    private static final String SUBSCRIBE_SUFFIX_POST_ID = "}";
    private static final StringBuilder SUBSCRIBE_STRING_BUILDER = new StringBuilder(SUBSCRIBE_PREFIX);

    private static int REQUEST_ID = 1;

    private final ObjectLongHashMap<String> streamToLastUpdateId;
    private final BinanceJsonParserListener responseListener;
    private final JsonParser jsonParser;

    public BinanceWebSocketMarketDataHandler() throws Exception {
        super();

        this.logger = LogManager.getLogger(BinanceWebSocketMarketDataHandler.class.getSimpleName());
        this.uri = new URI(getWebSocketEndpoint());
        this.quoteTopicToTradingPair = generateTopicToTradingPair(DynamoDbUtil.getMarketDataQuoteTopics(getExchange().getExchangeName()));
        this.candlestickTopicToTradingPair = generateTopicToTradingPair(DynamoDbUtil.getMarketDataCandlestickTopics(getExchange().getExchangeName()));
        this.quoteMap = ChronicleUtil.getQuoteMap(getExchange(), TradingPair.BTCUSDT);
        this.candlestickMaps = new HashMap<>();

        this.streamToLastUpdateId = new ObjectLongHashMap<>();
        this.responseListener = new BinanceJsonParserListener();
        this.jsonParser = new JsonParser(new FlyweightStringBuilder());
        this.jsonParser.setListener(this.responseListener);

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
            map.put(topic, TradingPair.EXCHANGE_TO_SYMBOL_TO_TRADING_PAIR.get(getExchange()).get(symbol.toUpperCase()));
        }

        return map;
    }

    @Override
    public void subscribe() {
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

        for (String topic : this.candlestickTopicToTradingPair.keySet()) {
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

        String stream = this.responseListener.stream.toString();
        long lastUpdateId = this.streamToLastUpdateId.get(stream);

        if (this.responseListener.lastUpdateId > lastUpdateId) {
            this.streamToLastUpdateId.put(stream, this.responseListener.lastUpdateId);

            TradingPair tradingPair = this.quoteTopicToTradingPair.get(stream);

            if (tradingPair == null) {
                tradingPair = this.candlestickTopicToTradingPair.get(stream);
            }

            if (tradingPair == null) return;

            if (this.responseListener.lastUpdateId > 0) {
                this.quote.setTradingPair(tradingPair);
                this.quote.setBidPrice(this.responseListener.bidPrice);
                this.quote.setBidSize(this.responseListener.bidSize);
                this.quote.setAskPrice(this.responseListener.askPrice);
                this.quote.setAskSize(this.responseListener.askSize);

                this.quoteMap.put(tradingPair, this.quote);
            } else {
                CandlestickInterval candlestickInterval = CandlestickInterval.INTERVALS_BY_DESCRIPTION.get(this.responseListener.interval);

                this.candlestick.setCandlestickInterval(candlestickInterval);
                this.candlestick.setOpenTime(this.candlestick.getOpenTime());
                this.candlestick.setCloseTime(this.candlestick.getCloseTime());
                this.candlestick.setOpenPrice(this.candlestick.getOpenPrice());
                this.candlestick.setClosePrice(this.candlestick.getClosePrice());

                this.candlestickMaps.computeIfAbsent(candlestickInterval, c -> {
                    try {
                        return ChronicleUtil.getCandlestickMap(getExchange(), candlestickInterval, TradingPair.BTCUSDT);
                    } catch (Exception e) {
                        this.logger.error("Could not initialize chronicle map for candlesticks.");
                    }
                    return null;
                }).put(tradingPair, this.candlestick);
            }
        }
    }
}
