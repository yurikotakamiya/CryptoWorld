package cw.feedhandler.binance;

import cw.common.json.FlyweightStringBuilder;
import cw.common.json.JsonParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BinanceQuoteJsonParserListenerTest {
    @Test
    void testFields() {
        String s1 = "{\"stream\":\"btcusdt@depth5@1000ms\",\"data\":{\"lastUpdateId\":15811478279,\"bids\":[[\"48378.69000000\",\"0.87476000\"],[\"48378.57000000\",\"0.01753000\"],[\"48375.86000000\",\"0.03064000\"],[\"48375.85000000\",\"0.05165000\"],[\"48375.30000000\",\"0.01000000\"]],\"asks\":[[\"48378.70000000\",\"0.01072000\"],[\"48382.32000000\",\"0.01431000\"],[\"48382.33000000\",\"0.04076000\"],[\"48385.70000000\",\"0.16268000\"],[\"48385.71000000\",\"0.02648000\"]]}}";
        String s2 = "{\"stream\":\"ethusdt@depth5@1000ms\",\"data\":{\"lastUpdateId\":13420498644,\"bids\":[[\"3952.70000000\",\"0.00510000\"],[\"3952.47000000\",\"0.49810000\"],[\"3952.46000000\",\"1.22610000\"],[\"3952.42000000\",\"5.41890000\"],[\"3952.32000000\",\"1.73140000\"]],\"asks\":[[\"3952.71000000\",\"2.10080000\"],[\"3952.81000000\",\"0.12650000\"],[\"3952.97000000\",\"0.66390000\"],[\"3953.14000000\",\"0.45070000\"],[\"3953.23000000\",\"3.65660000\"]]}}";

        JsonParser parser = new JsonParser(new FlyweightStringBuilder());
        BinanceQuoteJsonParserListener listener = new BinanceQuoteJsonParserListener();
        parser.setListener(listener);

        parser.parse(s1);
        parser.eoj();

        Assertions.assertEquals("btcusdt@depth5@1000ms", listener.stream.toString());
        Assertions.assertEquals(15811478279L, listener.lastUpdateId);
        Assertions.assertEquals(48378.69, listener.bidPrice);
        Assertions.assertEquals(0.87476, listener.bidSize);
        Assertions.assertEquals(48378.70, listener.askPrice);
        Assertions.assertEquals(0.01072, listener.askSize);

        parser.parse(s2);
        parser.eoj();

        Assertions.assertEquals("ethusdt@depth5@1000ms", listener.stream.toString());
        Assertions.assertEquals(13420498644L, listener.lastUpdateId);
        Assertions.assertEquals(3952.70, listener.bidPrice);
        Assertions.assertEquals(0.00510, listener.bidSize);
        Assertions.assertEquals(3952.71, listener.askPrice);
        Assertions.assertEquals(2.10080, listener.askSize);
    }
}
