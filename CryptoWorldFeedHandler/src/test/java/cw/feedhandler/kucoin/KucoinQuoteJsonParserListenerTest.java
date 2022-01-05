package cw.feedhandler.kucoin;

import cw.common.json.FlyweightStringBuilder;
import cw.common.json.JsonParser;

public class KucoinQuoteJsonParserListenerTest {
    public static void main(String[] args) {
        String s1 = "{\"type\":\"message\",\"topic\":\"/market/ticker:BTC-USDT\",\"subject\":\"trade.ticker\",\"data\":{\"bestAsk\":\"46362.5\",\"bestAskSize\":\"0.1090416\",\"bestBid\":\"46362.4\",\"bestBidSize\":\"0.1453809\",\"price\":\"46362.4\",\"sequence\":\"1624470279956\",\"size\":\"0.00002164\",\"time\":1641329461611}}";
        String s2 = "{\"type\":\"message\",\"topic\":\"/market/ticker:BTC-USDT\",\"subject\":\"trade.ticker\",\"data\":{\"bestAsk\":\"46459.9\",\"bestAskSize\":\"0.0347963\",\"bestBid\":\"46459.8\",\"bestBidSize\":\"0.60034341\",\"price\":\"46459.9\",\"sequence\":\"1624477527554\",\"size\":\"0.00109099\",\"time\":1641353005911}}";

        JsonParser parser = new JsonParser(new FlyweightStringBuilder());
        KucoinQuoteJsonParserListener listener = new KucoinQuoteJsonParserListener();
        parser.setListener(listener);

        parser.parse(s1);
        parser.eoj();

        System.out.println("market: " + listener.market);
        System.out.println("bidPrice: " + listener.bid);
        System.out.println("bidSize: " + listener.bidSize);
        System.out.println("askPrice: " + listener.ask);
        System.out.println("askSize: " + listener.askSize);
        System.out.println("time: " + listener.time);

        parser.parse(s2);
        parser.eoj();

        System.out.println("market: " + listener.market);
        System.out.println("bidPrice: " + listener.bid);
        System.out.println("bidSize: " + listener.bidSize);
        System.out.println("askPrice: " + listener.ask);
        System.out.println("askSize: " + listener.askSize);
        System.out.println("time: " + listener.time);

    }
}
