package cw.feedhandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

public class MdWebSocketClient extends WebSocketClient {
    private static final Logger LOGGER = LogManager.getLogger(MdWebSocketClient.class.getSimpleName());
    private AbstractWebSocketMarketDataHandler webSocketMarketDataHandler;

    public MdWebSocketClient(AbstractWebSocketMarketDataHandler webSocketMarketDataHandler) {
        super(webSocketMarketDataHandler.uri);

        this.webSocketMarketDataHandler = webSocketMarketDataHandler;
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        LOGGER.info("Connection opened.");
        this.webSocketMarketDataHandler.subscribe();
    }

    @Override
    public void onMessage(String s) {
        // TODO - remove the print statement
        System.out.println(s);
        this.webSocketMarketDataHandler.processMessage(s);
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        LOGGER.info("Connection closed.");
    }

    @Override
    public void onError(Exception e) {
        LOGGER.error("Error occurred.", e);
    }
}
