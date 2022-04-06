package cw.common.md;

import cw.common.db.mysql.TradingPair;
import net.openhft.chronicle.bytes.Byteable;
import net.openhft.chronicle.bytes.BytesStore;
import net.openhft.chronicle.values.NotNull;
import net.openhft.chronicle.values.Values;

public interface Quote extends Byteable {
    static Quote getNativeObject() {
        Quote quote = Values.newNativeReference(Quote.class);
        BytesStore store = BytesStore.nativeStoreWithFixedCapacity(quote.maxSize());
        quote.bytesStore(store, 0, quote.maxSize());
        return quote;
    }

    TradingPair getTradingPair();

    void setTradingPair(@NotNull TradingPair tradingPair);

    double getBidPrice();

    void setBidPrice(double bidPrice);

    double getAskPrice();

    void setAskPrice(double askPrice);

    double getBidSize();

    void setBidSize(double bidSize);

    double getAskSize();

    void setAskSize(double askSize);
}
