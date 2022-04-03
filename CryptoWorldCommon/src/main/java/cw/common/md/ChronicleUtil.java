package cw.common.md;

import cw.common.env.EnvUtil;
import cwp.db.dynamodb.DynamoDbUtil;
import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;

import java.io.File;

public class ChronicleUtil {
    public static ChronicleMap<TradingPair, Quote> getQuoteMap(Exchange exchange, TradingPair tradingPair) throws Exception {
        String marketDataMap = DynamoDbUtil.getMarketDataMap(exchange.getExchangeName(), EnvUtil.ENV.getEnvName());
        return ChronicleMapBuilder
                .of(TradingPair.class, Quote.class)
                .name(marketDataMap)
                .averageKey(tradingPair)
                .entries(10)
                .createPersistedTo(new File(marketDataMap));
    }

    public static Quote getQuote() {
        return Quote.getNativeObject();
    }
}
