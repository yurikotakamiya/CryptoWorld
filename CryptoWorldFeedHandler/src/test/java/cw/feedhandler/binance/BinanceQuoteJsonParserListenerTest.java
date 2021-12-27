package cw.feedhandler.binance;

import cw.common.json.FlyweightStringBuilder;
import cw.common.json.JsonParser;

public class BinanceQuoteJsonParserListenerTest {
    public static void main(String[] args) {
        String s1 = "{\"stream\":\"btcusdt@depth5@1000ms\",\"data\":{\"lastUpdateId\":15811478279,\"bids\":[[\"48378.69000000\",\"0.87476000\"],[\"48378.57000000\",\"0.01753000\"],[\"48375.86000000\",\"0.03064000\"],[\"48375.85000000\",\"0.05165000\"],[\"48375.30000000\",\"0.01000000\"]],\"asks\":[[\"48378.70000000\",\"0.01072000\"],[\"48382.32000000\",\"0.01431000\"],[\"48382.33000000\",\"0.04076000\"],[\"48385.70000000\",\"0.16268000\"],[\"48385.71000000\",\"0.02648000\"]]}}";
        String s2 = "{\"stream\":\"ethusdt@depth5@1000ms\",\"data\":{\"lastUpdateId\":13420498644,\"bids\":[[\"3952.70000000\",\"0.00510000\"],[\"3952.47000000\",\"0.49810000\"],[\"3952.46000000\",\"1.22610000\"],[\"3952.42000000\",\"5.41890000\"],[\"3952.32000000\",\"1.73140000\"]],\"asks\":[[\"3952.71000000\",\"2.10080000\"],[\"3952.81000000\",\"0.12650000\"],[\"3952.97000000\",\"0.66390000\"],[\"3953.14000000\",\"0.45070000\"],[\"3953.23000000\",\"3.65660000\"]]}}";
        String s3 = "{\"stream\":\"btcusdt@depth5@1000ms\",\"data\":{\"lastUpdateId\":15811478415,\"bids\":[[\"48385.69000000\",\"0.03649000\"],[\"48378.96000000\",\"0.01653000\"],[\"48378.93000000\",\"0.02048000\"],[\"48378.91000000\",\"0.04676000\"],[\"48378.70000000\",\"0.14776000\"]],\"asks\":[[\"48385.70000000\",\"2.50657000\"],[\"48385.72000000\",\"0.01845000\"],[\"48387.22000000\",\"0.04076000\"],[\"48389.20000000\",\"0.10215000\"],[\"48389.21000000\",\"0.04076000\"]]}}";
        String s4 = "{\"stream\":\"ethusdt@depth5@1000ms\",\"data\":{\"lastUpdateId\":13420498815,\"bids\":[[\"3952.96000000\",\"0.00010000\"],[\"3952.71000000\",\"0.03740000\"],[\"3952.70000000\",\"0.00510000\"],[\"3952.47000000\",\"0.49810000\"],[\"3952.46000000\",\"1.22610000\"]],\"asks\":[[\"3952.97000000\",\"7.35860000\"],[\"3953.22000000\",\"0.66740000\"],[\"3953.23000000\",\"2.60000000\"],[\"3953.24000000\",\"11.60000000\"],[\"3953.25000000\",\"2.95440000\"]]}}";
        String s5 = "{\"stream\":\"btcusdt@depth5@1000ms\",\"data\":{\"lastUpdateId\":15811478492,\"bids\":[[\"48385.69000000\",\"0.49589000\"],[\"48382.64000000\",\"0.01000000\"],[\"48382.00000000\",\"0.03000000\"],[\"48381.71000000\",\"0.01408000\"],[\"48381.70000000\",\"0.02000000\"]],\"asks\":[[\"48385.70000000\",\"0.83330000\"],[\"48385.72000000\",\"0.01845000\"],[\"48387.22000000\",\"0.04076000\"],[\"48389.19000000\",\"0.02060000\"],[\"48389.20000000\",\"0.10215000\"]]}}";
        String s6 = "{\"stream\":\"ethusdt@depth5@1000ms\",\"data\":{\"lastUpdateId\":13420499013,\"bids\":[[\"3952.49000000\",\"21.65950000\"],[\"3952.48000000\",\"5.82060000\"],[\"3952.21000000\",\"0.50600000\"],[\"3952.20000000\",\"0.57000000\"],[\"3952.02000000\",\"1.72270000\"]],\"asks\":[[\"3952.50000000\",\"0.12650000\"],[\"3952.96000000\",\"3.85380000\"],[\"3952.97000000\",\"3.64800000\"],[\"3953.22000000\",\"0.66740000\"],[\"3953.25000000\",\"2.95440000\"]]}}";

        JsonParser parser = new JsonParser(new FlyweightStringBuilder());
        BinanceQuoteJsonParserListener listener = new BinanceQuoteJsonParserListener();
        parser.setListener(listener);

        JsonParser.Next next = parser.parse(s1);
        while (next != null) {
            next = next.next();
        }
        parser.eoj();

        System.out.println(listener.stream.toString());
        System.out.println(listener.lastUpdateId);
        System.out.println(listener.bidPrice);
        System.out.println(listener.bidSize);
        System.out.println(listener.askPrice);
        System.out.println(listener.askSize);

        next = parser.parse(s2);
        while (next != null) {
            next = next.next();
        }
        parser.eoj();

        System.out.println(listener.stream.toString());
        System.out.println(listener.lastUpdateId);
        System.out.println(listener.bidPrice);
        System.out.println(listener.bidSize);
        System.out.println(listener.askPrice);
        System.out.println(listener.askSize);

        next = parser.parse(s3);
        while (next != null) {
            next = next.next();
        }
        parser.eoj();

        System.out.println(listener.stream.toString());
        System.out.println(listener.lastUpdateId);
        System.out.println(listener.bidPrice);
        System.out.println(listener.bidSize);
        System.out.println(listener.askPrice);
        System.out.println(listener.askSize);

        next = parser.parse(s4);
        while (next != null) {
            next = next.next();
        }
        parser.eoj();

        System.out.println(listener.stream.toString());
        System.out.println(listener.lastUpdateId);
        System.out.println(listener.bidPrice);
        System.out.println(listener.bidSize);
        System.out.println(listener.askPrice);
        System.out.println(listener.askSize);

        next = parser.parse(s5);
        while (next != null) {
            next = next.next();
        }
        parser.eoj();

        System.out.println(listener.stream.toString());
        System.out.println(listener.lastUpdateId);
        System.out.println(listener.bidPrice);
        System.out.println(listener.bidSize);
        System.out.println(listener.askPrice);
        System.out.println(listener.askSize);

        next = parser.parse(s6);
        while (next != null) {
            next = next.next();
        }
        parser.eoj();

        System.out.println(listener.stream.toString());
        System.out.println(listener.lastUpdateId);
        System.out.println(listener.bidPrice);
        System.out.println(listener.bidSize);
        System.out.println(listener.askPrice);
        System.out.println(listener.askSize);
    }
}
