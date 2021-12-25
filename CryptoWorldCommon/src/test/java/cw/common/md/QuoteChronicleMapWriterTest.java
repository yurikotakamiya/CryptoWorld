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

        // Create a reference and allocate a ByteStore to use as Quote
        Quote q = Quote.getNativeObject();

        while (true) {
            // Be sure to use the same instance of the String to populate the TradingPair
            q.setTradingPair(TradingPair.BTCUSDT);
            q.setBidPrice(seq + 0.01);
            q.setAskPrice(seq + 0.02);
            q.setBidSize(seq);
            q.setAskSize(seq);

            // Populate quotes in the map
            map.put(q.getTradingPair(), q);

            // Be sure to use the same instance of the String to populate the TradingPair
            q.setTradingPair(TradingPair.ETHUSDT);
            q.setBidPrice(seq + 0.11);
            q.setAskPrice(seq + 0.22);
            q.setBidSize(seq);
            q.setAskSize(seq);

            // Populate quotes in the map
            map.put(q.getTradingPair(), q);

            // Testing whether get would allocate new objects on heap and to avoid it, be sure to use getUsing
            q = map.getUsing(q.getTradingPair(), q);
            q = map.getUsing(q.getTradingPair(), q);

            seq++;

            // Every now and then, install debug points and inspect how memory allocation in between
            if (seq % 500 == 0) {
                System.out.println();
            }

            Thread.sleep(10);
        }
    }
}
