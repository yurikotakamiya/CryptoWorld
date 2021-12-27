package cw.feedhandler;

import cw.common.md.Exchange;
import cw.common.md.Quote;
import cw.common.md.TradingPair;
import net.openhft.chronicle.map.ChronicleMap;
import org.java_websocket.client.WebSocketClient;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class AbstractWebSocketMarketDataHandler implements Runnable {
    protected WebSocketClient webSocketClient;
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
            try {
                String s = this.queue.poll();
                if (s != null) {
                    processMessage(s);
                }
            } catch (Exception e) {
                // TODO - log
                e.printStackTrace();
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
        if (this.topicToTradingPair.isEmpty()) throw new Exception("No topics to subscribe to.");
    }
}
