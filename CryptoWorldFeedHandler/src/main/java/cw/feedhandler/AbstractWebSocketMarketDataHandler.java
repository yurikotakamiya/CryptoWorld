package cw.feedhandler;

import cw.common.md.Exchange;
import cw.common.md.Quote;
import cw.common.md.TradingPair;
import net.openhft.chronicle.map.ChronicleMap;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.java_websocket.client.WebSocketClient;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class AbstractWebSocketMarketDataHandler implements Runnable {
    protected WebSocketClient webSocketClient;
    protected Logger logger;
    protected URI uri;
    protected Map<String, TradingPair> topicToTradingPair;
    protected ChronicleMap<TradingPair, Quote> chronicleMap;

    private final ConcurrentLinkedQueue<String> queue;
    protected final Quote quoteNativeReference;

    public AbstractWebSocketMarketDataHandler() {
        this.queue = new ConcurrentLinkedQueue<>();
        this.quoteNativeReference = Quote.getNativeObject();
    }

    protected abstract String getWebSocketEndpoint();

    protected abstract Exchange getExchange();

    protected abstract Map<String, TradingPair> generateTopicToTradingPair(String[] topics);

    protected abstract void subscribe();

    protected abstract void processMessage(String message);

    @Override
    public void run() {
        connect();

        while (true) {
            String s = this.queue.poll();

            if (s != null) {
                try {
                    processMessage(s);
                } catch (Exception e) {
                    this.logger.error(new ParameterizedMessage("Exception occurred while handling message {}.", s), e);
                }
            }
        }
    }

    public void enqueueMessage(String message) {
        this.queue.add(message);
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
        if (this.topicToTradingPair.isEmpty()) throw new Exception("No topics to subscribe to.");
    }
}
