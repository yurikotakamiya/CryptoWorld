package cw.feedhandler.ftx;

import cw.common.json.FlyweightStringBuilder;
import cw.common.json.JsonParser;

public class FtxQuoteJsonParserListenerTest {
    public static void main(String[] args) {
        String s1 = "{\"channel\": \"ticker\", \"market\": \"ETH-PERP\", \"type\": \"update\", \"data\": {\"bid\": 3774.1, \"ask\": 3774.2, \"bidSize\": 39.709, \"askSize\": 4.631, \"last\": 3774.1, \"time\": 1640718434.1286812}}";
        String s2 = "{\"channel\": \"ticker\", \"market\": \"ETH-PERP\", \"type\": \"update\", \"data\": {\"bid\": 3774.2, \"ask\": 3774.3, \"bidSize\": 4.197, \"askSize\": 0.617, \"last\": 3774.3, \"time\": 1640718434.2122264}}";

        JsonParser parser = new JsonParser(new FlyweightStringBuilder());
        FtxQuoteJsonParserListener listener = new FtxQuoteJsonParserListener();
        parser.setListener(listener);

        parser.parse(s1);
        parser.eoj();

        System.out.println("market: " + listener.market);
        System.out.println("bidPrice: " + listener.bidPrice);
        System.out.println("bidSize: " + listener.bidSize);
        System.out.println("askPrice: " + listener.askPrice);
        System.out.println("askSize: " + listener.askSize);
        System.out.println("time: " + listener.time);

        parser.parse(s2);
        parser.eoj();

        System.out.println("market: " + listener.market);
        System.out.println("bidPrice: " + listener.bidPrice);
        System.out.println("bidSize: " + listener.bidSize);
        System.out.println("askPrice: " + listener.askPrice);
        System.out.println("askSize: " + listener.askSize);
        System.out.println("time: " + listener.time);
    }
}
