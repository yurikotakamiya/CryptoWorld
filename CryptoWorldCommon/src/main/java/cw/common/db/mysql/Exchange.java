package cw.common.db.mysql;

public enum Exchange {
    NONE("none"),
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
