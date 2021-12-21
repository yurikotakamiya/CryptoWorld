package cw.common.md;

import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;

import java.io.File;

public class QuoteChronicleMapReaderTest {
    private static final String BTC_USDT = "BTCUSDT";
    private static final String ETH_USDT = "ETHUSDT";

    public static void main(String[] args) throws Exception {
        ChronicleMap<CharSequence, Quote> map = ChronicleMapBuilder
                .of(CharSequence.class, Quote.class)
                .name("market_data_map")
                .averageKey(ETH_USDT)
                .entries(10)
                .createPersistedTo(new File("/tmp/market_data.dat"));

        // Use allocated ByteStores to avoid heap objects
        Quote q1 = Quote.getNativeObject();
        Quote q2 = Quote.getNativeObject();

        while (true) {
            q1 = map.getUsing(BTC_USDT, q1);
            q2 = map.getUsing(ETH_USDT, q2);

            // Interval for writing is much shorter than reading and this tests data does not corrupt in the middle of reads
            // System.out.println(q1.getTradingPair() + " " + q1.getBidSize() + " " + q1.getAskSize() + " " + q1.getBidPrice() + " " + q1.getAskPrice());
            // System.out.println(q2.getTradingPair() + " " + q2.getBidSize() + " " + q2.getAskSize() + " " + q2.getBidPrice() + " " + q2.getAskPrice());

            // When checking for memory changes, comment out the print lines above because it will create String objects
            Thread.sleep(1000);
        }
    }
}
