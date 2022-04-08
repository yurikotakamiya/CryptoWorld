package cw.feedhandler;

import cw.common.db.mysql.CandlestickInterval;
import cw.common.db.mysql.Exchange;
import cw.common.md.Candlestick;
import cw.common.md.Quote;
import cw.common.db.mysql.TradingPair;
import net.openhft.chronicle.map.ChronicleMap;
import org.apache.logging.log4j.Logger;
import org.java_websocket.client.WebSocketClient;

import java.net.URI;
import java.util.Map;

public abstract class AbstractWebSocketMarketDataHandler {
    protected WebSocketClient webSocketClient;
    protected Logger logger;
    protected URI uri;
    protected Map<String, TradingPair> quoteTopicToTradingPair;
    protected Map<String, TradingPair> candlestickTopicToTradingPair;
    protected ChronicleMap<TradingPair, Quote> quoteMap;
    protected Map<CandlestickInterval, ChronicleMap<TradingPair, Candlestick>> candlestickMaps;

    protected final Quote quote;
    protected final Candlestick candlestick;

    public AbstractWebSocketMarketDataHandler() {
        this.quote = Quote.getNativeObject();
        this.candlestick = Candlestick.getNativeObject();
    }

    protected abstract String getWebSocketEndpoint() throws Exception;

    protected abstract Exchange getExchange();

    protected abstract Map<String, TradingPair> generateTopicToTradingPair(String[] topics);

    protected abstract void subscribe();

    protected abstract void processMessage(String message);

    protected void throttleRequest() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            this.logger.error("Error while throttling request.", e);
        }
    }

    protected void connect() {
        this.webSocketClient = new MdWebSocketClient(this);
        this.webSocketClient.connect();
    }

    protected void disconnect() {
        this.webSocketClient.close();
        this.webSocketClient = null;
    }

    protected void validate() throws Exception {
        if (this.logger == null) throw new Exception("No logger has been created.");
        if (this.quoteTopicToTradingPair.isEmpty()) throw new Exception("No topics to subscribe to.");
    }
}
