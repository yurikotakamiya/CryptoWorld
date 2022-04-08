package cw.feedhandler.binance;

import cw.common.json.FlyweightStringBuilder;
import cw.common.json.JsonParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BinanceJsonParserListenerTest {
    @Test
    void testFields_Quote() {
        String s1 = "{\"stream\":\"btcusdt@depth5@1000ms\",\"data\":{\"lastUpdateId\":15811478279,\"bids\":[[\"48378.69000000\",\"0.87476000\"],[\"48378.57000000\",\"0.01753000\"],[\"48375.86000000\",\"0.03064000\"],[\"48375.85000000\",\"0.05165000\"],[\"48375.30000000\",\"0.01000000\"]],\"asks\":[[\"48378.70000000\",\"0.01072000\"],[\"48382.32000000\",\"0.01431000\"],[\"48382.33000000\",\"0.04076000\"],[\"48385.70000000\",\"0.16268000\"],[\"48385.71000000\",\"0.02648000\"]]}}";
        String s2 = "{\"stream\":\"ethusdt@depth5@1000ms\",\"data\":{\"lastUpdateId\":13420498644,\"bids\":[[\"3952.70000000\",\"0.00510000\"],[\"3952.47000000\",\"0.49810000\"],[\"3952.46000000\",\"1.22610000\"],[\"3952.42000000\",\"5.41890000\"],[\"3952.32000000\",\"1.73140000\"]],\"asks\":[[\"3952.71000000\",\"2.10080000\"],[\"3952.81000000\",\"0.12650000\"],[\"3952.97000000\",\"0.66390000\"],[\"3953.14000000\",\"0.45070000\"],[\"3953.23000000\",\"3.65660000\"]]}}";

        JsonParser parser = new JsonParser(new FlyweightStringBuilder());
        BinanceJsonParserListener listener = new BinanceJsonParserListener();
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

    @Test
    void testFields_Candlestick() {
        String s1 = "{\"stream\":\"btcusdt@kline_1h\",\"data\":{\"e\":\"kline\",\"E\":1649372533879,\"s\":\"BTCUSDT\",\"k\":{\"t\":1649372400000,\"T\":1649375999999,\"s\":\"BTCUSDT\",\"i\":\"1h\",\"f\":1319524715,\"L\":1319525968,\"o\":\"43629.72000000\",\"c\":\"43617.27000000\",\"h\":\"43629.73000000\",\"l\":\"43591.40000000\",\"v\":\"37.54740000\",\"n\":1254,\"x\":false,\"q\":\"1637596.66998250\",\"V\":\"12.42352000\",\"Q\":\"541754.44672960\",\"B\":\"0\"}}}";
        String s2 = "{\"stream\":\"btcusdt@kline_1h\",\"data\":{\"e\":\"kline\",\"E\":1649372535955,\"s\":\"BTCUSDT\",\"k\":{\"t\":1649372400000,\"T\":1649375999999,\"s\":\"BTCUSDT\",\"i\":\"1h\",\"f\":1319524715,\"L\":1319525988,\"o\":\"43629.72000000\",\"c\":\"43620.12000000\",\"h\":\"43629.73000000\",\"l\":\"43591.40000000\",\"v\":\"37.76789000\",\"n\":1274,\"x\":false,\"q\":\"1647214.27218430\",\"V\":\"12.53791000\",\"Q\":\"546743.95419940\",\"B\":\"0\"}}}";

        JsonParser parser = new JsonParser(new FlyweightStringBuilder());
        BinanceJsonParserListener listener = new BinanceJsonParserListener();
        parser.setListener(listener);

        parser.parse(s1);
        parser.eoj();

        Assertions.assertEquals("btcusdt@kline_1h", listener.stream.toString());
        Assertions.assertEquals("1h", listener.interval);
        Assertions.assertEquals(1649372400000L, listener.openTime);
        Assertions.assertEquals(1649375999999L, listener.closeTime);
        Assertions.assertEquals(43629.72, listener.openPrice);
        Assertions.assertEquals(43617.27, listener.closePrice);

        parser.parse(s2);
        parser.eoj();

        Assertions.assertEquals("btcusdt@kline_1h", listener.stream.toString());
        Assertions.assertEquals("1h", listener.interval);
        Assertions.assertEquals(1649372400000L, listener.openTime);
        Assertions.assertEquals(1649375999999L, listener.closeTime);
        Assertions.assertEquals(43629.72, listener.openPrice);
        Assertions.assertEquals(43620.12, listener.closePrice);
    }
}
