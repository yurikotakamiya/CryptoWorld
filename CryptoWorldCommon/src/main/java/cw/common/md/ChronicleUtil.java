package cw.common.md;

import cw.common.db.mysql.CandlestickInterval;
import cw.common.db.mysql.Exchange;
import cw.common.db.mysql.TradingPair;
import cw.common.env.EnvUtil;
import cwp.db.dynamodb.DynamoDbUtil;
import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;

import java.io.File;

public class ChronicleUtil {
    public static ChronicleMap<TradingPair, Quote> getQuoteMap(Exchange exchange, TradingPair tradingPair) throws Exception {
        String marketDataMap = DynamoDbUtil.getMarketDataQuoteMap(exchange.getExchangeName(), EnvUtil.ENV.getEnvName());
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

    public static ChronicleMap<TradingPair, Candlestick> getCandlestickMap(Exchange exchange, CandlestickInterval interval, TradingPair tradingPair) throws Exception {
        String marketDataMap = DynamoDbUtil.getMarketDataCandlestickMap(exchange.getExchangeName(), interval.getInterval(), EnvUtil.ENV.getEnvName());
        return ChronicleMapBuilder
                .of(TradingPair.class, Candlestick.class)
                .name(marketDataMap)
                .averageKey(tradingPair)
                .entries(10)
                .createPersistedTo(new File(marketDataMap));
    }

    public static Candlestick getCandlestick() {
        return Candlestick.getNativeObject();
    }
}
