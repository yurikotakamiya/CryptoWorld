package cw.feedhandler;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

public class MdWebSocketClient extends WebSocketClient {
    AbstractWebSocketMarketDataHandler webSocketMarketDataHandler;

    public MdWebSocketClient(AbstractWebSocketMarketDataHandler webSocketMarketDataHandler) {
        super(webSocketMarketDataHandler.uri);

        this.webSocketMarketDataHandler = webSocketMarketDataHandler;
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        this.webSocketMarketDataHandler.subscribe();

        // TODO - replace with log
        System.out.println("Connection opened.");
    }

    @Override
    public void onMessage(String s) {
        // TODO - remove the print statement
        System.out.println(s);
        this.webSocketMarketDataHandler.enqueueMessage(s);
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        // TODO - replace with log
        System.out.println("Connection closed.");
    }

    @Override
    public void onError(Exception e) {
        // TODO - replace with log
        System.out.println("Error occurred.");
    }
}
