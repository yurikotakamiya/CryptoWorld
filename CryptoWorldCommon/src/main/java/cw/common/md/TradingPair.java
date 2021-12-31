package cw.common.md;

import java.util.HashMap;
import java.util.Map;

public enum TradingPair {
    BTCUSDT(Map.of(
            Exchange.BINANCE, "btcusdt",
            Exchange.KUCOIN, "BTC-USDT")),
    ETHUSDT(Map.of(
            Exchange.BINANCE, "ethusdt",
            Exchange.KUCOIN, "ETH-USDT")),
    BTCPERP(Map.of(
            Exchange.FTX, "BTC-PERP")),
    ETHPERP(Map.of(
            Exchange.FTX, "ETH-PERP"));

    public static final Map<Exchange, Map<String, TradingPair>> EXCHANGE_TO_SYMBOL_TO_TRADING_PAIR;

    static {
        EXCHANGE_TO_SYMBOL_TO_TRADING_PAIR = new HashMap<>();

        for (TradingPair tradingPair : TradingPair.values()) {
            for (Map.Entry<Exchange, String> entry : tradingPair.getExchangeToSymbolMap().entrySet()) {
                Map<String, TradingPair> symbolToTradingPair = EXCHANGE_TO_SYMBOL_TO_TRADING_PAIR.computeIfAbsent(entry.getKey(), e -> new HashMap<>());
                symbolToTradingPair.put(entry.getValue(), tradingPair);
            }
        }
    }

    private final Map<Exchange, String> exchangeToSymbolMap;

    TradingPair(Map<Exchange, String> exchangeToSymbolMap) {
        this.exchangeToSymbolMap = exchangeToSymbolMap;
    }

    public Map<Exchange, String> getExchangeToSymbolMap() {
        return this.exchangeToSymbolMap;
    }
}
