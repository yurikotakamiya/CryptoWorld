package cw.common.md;

import net.openhft.chronicle.bytes.Byteable;
import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.values.MaxUtf8Length;
import net.openhft.chronicle.values.Values;

public interface Quote extends Byteable {
    static BytesStore getNativeReference() {
        Quote quote = Values.newNativeReference(Quote.class);
        BytesStore store = BytesStore.nativeStoreWithFixedCapacity(quote.maxSize());
        return store;
    }

    CharSequence getSymbol();

    void setSymbol(@MaxUtf8Length(12) CharSequence symbol);

    double getBidPrice();

    void setBidPrice(double bidPrice);

    double getAskPrice();

    void setAskPrice(double askPrice);

    double getBidSize();

    void setBidSize(double bidSize);

    double getAskSize();

    void setAskSize(double askSize);
}
