package cw.common.db.mysql;

import java.io.Serializable;
import java.util.Objects;

public class StrategyConfigId implements Serializable {
    private int userId;
    private byte exchange;
    private byte tradingPair;
    private byte strategy;

    public StrategyConfigId() {
    }

    public StrategyConfigId(int userId, byte exchange, byte tradingPair, byte strategy) {
        this.userId = userId;
        this.exchange = exchange;
        this.tradingPair = tradingPair;
        this.strategy = strategy;
    }

    public int getUserId() {
        return this.userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public byte getExchange() {
        return this.exchange;
    }

    public void setExchange(byte exchange) {
        this.exchange = exchange;
    }

    public byte getTradingPair() {
        return this.tradingPair;
    }

    public void setTradingPair(byte tradingPair) {
        this.tradingPair = tradingPair;
    }

    public byte getStrategy() {
        return this.strategy;
    }

    public void setStrategy(byte strategy) {
        this.strategy = strategy;
    }

    @Override
    public String toString() {
        return "StrategyConfigId{" +
                "userId=" + userId +
                ", exchange=" + exchange +
                ", tradingPair=" + tradingPair +
                ", strategy=" + strategy +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StrategyConfigId that = (StrategyConfigId) o;
        return userId == that.userId && exchange == that.exchange && tradingPair == that.tradingPair && strategy == that.strategy;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, exchange, tradingPair, strategy);
    }
}
