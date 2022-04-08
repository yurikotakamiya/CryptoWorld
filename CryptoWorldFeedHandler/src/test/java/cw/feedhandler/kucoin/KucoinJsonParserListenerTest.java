package cw.feedhandler.kucoin;

import cw.common.json.FlyweightStringBuilder;
import cw.common.json.JsonParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class KucoinJsonParserListenerTest {
    @Test
    void testFields() {
        String s1 = "{\"type\":\"message\",\"topic\":\"/market/ticker:BTC-USDT\",\"subject\":\"trade.ticker\",\"data\":{\"bestAsk\":\"46362.5\",\"bestAskSize\":\"0.1090416\",\"bestBid\":\"46362.4\",\"bestBidSize\":\"0.1453809\",\"price\":\"46362.4\",\"sequence\":\"1624470279956\",\"size\":\"0.00002164\",\"time\":1641329461611}}";
        String s2 = "{\"type\":\"message\",\"topic\":\"/market/ticker:BTC-USDT\",\"subject\":\"trade.ticker\",\"data\":{\"bestAsk\":\"46459.9\",\"bestAskSize\":\"0.0347963\",\"bestBid\":\"46459.8\",\"bestBidSize\":\"0.60034341\",\"price\":\"46459.9\",\"sequence\":\"1624477527554\",\"size\":\"0.00109099\",\"time\":1641353005911}}";

        JsonParser parser = new JsonParser(new FlyweightStringBuilder());
        KucoinJsonParserListener listener = new KucoinJsonParserListener();
        parser.setListener(listener);

        parser.parse(s1);
        parser.eoj();

        Assertions.assertEquals("/market/ticker:BTC-USDT", listener.market);
        Assertions.assertEquals(46362.4, listener.bid);
        Assertions.assertEquals(0.1453809, listener.bidSize);
        Assertions.assertEquals(46362.5, listener.ask);
        Assertions.assertEquals(0.1090416, listener.askSize);
        Assertions.assertEquals(1641329461611L, listener.time);

        parser.parse(s2);
        parser.eoj();

        Assertions.assertEquals("/market/ticker:BTC-USDT", listener.market);
        Assertions.assertEquals(46459.8, listener.bid);
        Assertions.assertEquals(0.60034341, listener.bidSize);
        Assertions.assertEquals(46459.9, listener.ask);
        Assertions.assertEquals(0.0347963, listener.askSize);
        Assertions.assertEquals(1641353005911L, listener.time);
    }
}
