package cw.feedhandler;

import cw.common.md.Exchange;
import cw.common.md.Quote;
import cw.common.md.TradingPair;
import net.openhft.chronicle.map.ChronicleMap;
import org.apache.logging.log4j.Logger;
import org.java_websocket.client.WebSocketClient;

import java.net.URI;
import java.util.Map;

public abstract class AbstractWebSocketMarketDataHandler {
    protected WebSocketClient webSocketClient;
    protected Logger logger;
    protected URI uri;
    protected Map<String, TradingPair> topicToTradingPair;
    protected ChronicleMap<TradingPair, Quote> chronicleMap;

    protected final Quote quoteNativeReference;

    public AbstractWebSocketMarketDataHandler() {
        this.quoteNativeReference = Quote.getNativeObject();
    }

    protected abstract String getWebSocketEndpoint();

    protected abstract Exchange getExchange();

    protected abstract Map<String, TradingPair> generateTopicToTradingPair(String[] topics);

    protected abstract void subscribe();

    protected abstract void processMessage(String message);

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
        if (this.topicToTradingPair.isEmpty()) throw new Exception("No topics to subscribe to.");
    }
}
