package cw.common.md;

public enum Exchange {
    BINANCE("binance"),
    KUCOIN("kucoin"),
    FTX("ftx");

    private final String exchangeName;

    Exchange(String exchangeName) {
        this.exchangeName = exchangeName;
    }

    public String getExchangeName() {
        return this.exchangeName;
    }
}
