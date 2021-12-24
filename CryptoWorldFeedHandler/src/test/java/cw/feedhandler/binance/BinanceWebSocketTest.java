package cw.feedhandler.binance;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;

public class BinanceWebSocketTest {
    public static void main(String[] args) throws URISyntaxException, InterruptedException {
        WebSocketClient webSocketClient = new WebSocketClient(new URI("wss://stream.binance.com:9443/stream")) {
            @Override
            public void onMessage(String message) {
                System.out.println(message);
            }

            @Override
            public void onOpen(ServerHandshake handshake) {
                System.out.println("Connection opened.");
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                System.out.println("Connection closed.");
            }

            @Override
            public void onError(Exception ex) {
                ex.printStackTrace();
            }
        };

        // Open web socket connection
        webSocketClient.connect();
        Thread.sleep(3000);

        // Subscribe to market data
        String request = "{\"method\": \"SUBSCRIBE\", \"params\": [\"btcusdt@depth5@1000ms\"\n],\n \"id\": 1\n}";
        webSocketClient.send(request);

        // Listen to market data for 10 seconds
        Thread.sleep(10000);

        // Unsubscribe from market data
        request = "{\"method\": \"UNSUBSCRIBE\", \"params\": [\"btcusdt@depth5@1000ms\"\n],\n \"id\": 2\n}";
        webSocketClient.send(request);

        // Close the web socket connection
        webSocketClient.close();
    }
}
