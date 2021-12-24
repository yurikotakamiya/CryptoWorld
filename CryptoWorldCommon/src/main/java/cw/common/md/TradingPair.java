package cw.common.md;

import java.util.Map;

public enum TradingPair {
    BTCUSDT(Map.of(
            Exchange.BINANCE, "btcusdt",
            Exchange.KUCOIN, "BTC-USDT")),
    ETHUSDT(Map.of(
            Exchange.BINANCE, "ethusdt",
            Exchange.KUCOIN, "ETH-USDT"));

    private final Map<Exchange, String> exchangeToSymbolMap;

    TradingPair(Map<Exchange, String> exchangeToSymbolMap) {
        this.exchangeToSymbolMap = exchangeToSymbolMap;
    }

    public Map<Exchange, String> getExchangeToSymbolMap() {
        return this.exchangeToSymbolMap;
    }
}
