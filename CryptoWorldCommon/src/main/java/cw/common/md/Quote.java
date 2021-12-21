package cw.common.md;

import net.openhft.chronicle.bytes.Byteable;
import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.values.MaxUtf8Length;
import net.openhft.chronicle.values.Values;

public interface Quote extends Byteable {
    static Quote getNativeObject() {
        Quote quote = Values.newNativeReference(Quote.class);
        BytesStore store = BytesStore.nativeStoreWithFixedCapacity(quote.maxSize());
        quote.bytesStore(store, 0, quote.maxSize());
        return quote;
    }

    CharSequence getTradingPair();

    void setTradingPair(@MaxUtf8Length(12) CharSequence tradingPair);

    double getBidPrice();

    void setBidPrice(double bidPrice);

    double getAskPrice();

    void setAskPrice(double askPrice);

    double getBidSize();

    void setBidSize(double bidSize);

    double getAskSize();

    void setAskSize(double askSize);
}
