package cw.feedhandler.kucoin;

import com.google.gson.Gson;
import cw.feedhandler.kucoin.gson.Response;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.stream.Collectors;

public class KucoinWebSocketTest {
    public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException {
        URL url = new URL("https://api.kucoin.com/api/v1/bullet-public");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");

        BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String input = reader.lines().collect(Collectors.joining());
        reader.close();

        Gson gson = new Gson();
        Response response = gson.fromJson(input, Response.class);
        String token = response.data.token;
        String endpoint = "wss://ws-api.kucoin.com/endpoint";

        URI uri = new URI(endpoint + "?token=" + token);
        WebSocketClient client = new WebSocketClient(uri) {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                System.out.println(serverHandshake);
            }

            @Override
            public void onMessage(String s) {
                System.out.println(s);
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                System.out.println(s);
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        };
        client.connect();
        Thread.sleep(3000);

        String request = "{\"id\": 1, \"type\": \"subscribe\",\"topic\":\"/market/ticker:BTC-USDT\",\"privateChannel\": false,\"response\": true }";
        client.send(request);

        Thread.sleep(5000);
        request = "{\"id\": 1, \"type\": \"unsubscribe\",\"topic\":\"/market/ticker:BTC-USDT\",\"privateChannel\": false,\"response\": true }";

        client.send(request);
        client.close();
    }
}
