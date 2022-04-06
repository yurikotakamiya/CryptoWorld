package cw.common.md;

import cw.common.db.mysql.TradingPair;
import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;

import java.io.File;

public class QuoteChronicleMapReaderTest {
    public static void main(String[] args) throws Exception {
        ChronicleMap<TradingPair, Quote> map = ChronicleMapBuilder
                .of(TradingPair.class, Quote.class)
                .name("market_data_map")
                .averageKey(TradingPair.ETHUSDT)
                .entries(10)
                .createPersistedTo(new File("/tmp/market_data.dat"));

        // Use allocated ByteStores to avoid heap objects
        Quote q1 = Quote.getNativeObject();
        Quote q2 = Quote.getNativeObject();

        while (true) {
            map.getUsing(TradingPair.BTCUSDT, q1);
            map.getUsing(TradingPair.ETHUSDT, q2);

            // Interval for writing is much shorter than reading and this tests data does not corrupt in the middle of reads
            // System.out.println(q1.getTradingPair() + " " + q1.getBidSize() + " " + q1.getAskSize() + " " + q1.getBidPrice() + " " + q1.getAskPrice());
            // System.out.println(q2.getTradingPair() + " " + q2.getBidSize() + " " + q2.getAskSize() + " " + q2.getBidPrice() + " " + q2.getAskPrice());

            // When checking for memory changes, comment out the print lines above because it will create String objects
            Thread.sleep(1000);
        }
    }
}
