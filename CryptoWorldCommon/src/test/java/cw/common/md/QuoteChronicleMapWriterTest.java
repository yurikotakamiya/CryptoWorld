package cw.common.md;

import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;

import java.io.File;

public class QuoteChronicleMapWriterTest {
    public static void main(String[] args) throws Exception {
        ChronicleMap<TradingPair, Quote> map = ChronicleMapBuilder
                .of(TradingPair.class, Quote.class)
                .name("market_data_map")
                .averageKey(TradingPair.ETHUSDT)
                .entries(10)
                .createPersistedTo(new File("/tmp/market_data.dat"));

        double seq = 0;

        // Create a reference and allocate a ByteStore to use as Quote 1
        Quote q1 = Quote.getNativeObject();
        q1.setTradingPair(TradingPair.BTCUSDT);
        q1.setBidPrice(seq + 0.01);
        q1.setAskPrice(seq + 0.02);
        q1.setBidSize(seq);
        q1.setAskSize(seq);

        // Create another pair of reference and ByteStore to use as Quote 2
        Quote q2 = Quote.getNativeObject();
        q2.setTradingPair(TradingPair.ETHUSDT);
        q2.setBidPrice(seq + 0.11);
        q2.setAskPrice(seq + 0.22);
        q2.setBidSize(seq);
        q2.setAskSize(seq);

        while (true) {
            // Be sure to use the same instance of the String to populate the TradingPair
            q1.setTradingPair(TradingPair.BTCUSDT);
            q1.setBidPrice(seq + 0.01);
            q1.setAskPrice(seq + 0.02);
            q1.setBidSize(seq);
            q1.setAskSize(seq);

            // Be sure to use the same instance of the String to populate the TradingPair
            q2.setTradingPair(TradingPair.ETHUSDT);
            q2.setBidPrice(seq + 0.11);
            q2.setAskPrice(seq + 0.22);
            q2.setBidSize(seq);
            q2.setAskSize(seq);

            // Populate quotes in the map
            map.put(q1.getTradingPair(), q1);
            map.put(q2.getTradingPair(), q2);

            // Testing whether get would allocate new objects on heap and to avoid it, be sure to use getUsing
            q1 = map.getUsing(q1.getTradingPair(), q1);
            q2 = map.getUsing(q2.getTradingPair(), q2);

            seq++;

            // Every now and then, install debug points and inspect how memory allocation in between
            if (seq % 500 == 0) {
                System.out.println();
            }

            Thread.sleep(10);
        }
    }
}
