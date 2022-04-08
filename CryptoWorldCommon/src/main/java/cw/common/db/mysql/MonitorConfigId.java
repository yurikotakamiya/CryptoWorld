package cw.common.db.mysql;

import java.io.Serializable;
import java.util.Objects;

public class MonitorConfigId implements Serializable {
    private int userId;
    private byte exchange;
    private byte tradingPair;
    private byte monitor;

    public MonitorConfigId() {
    }

    public MonitorConfigId(int userId, byte exchange, byte tradingPair, byte monitor) {
        this.userId = userId;
        this.exchange = exchange;
        this.tradingPair = tradingPair;
        this.monitor = monitor;
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

    public byte getMonitor() {
        return this.monitor;
    }

    public void setMonitor(byte monitor) {
        this.monitor = monitor;
    }

    @Override
    public String toString() {
        return "MonitorConfigId{" +
                "userId=" + userId +
                ", exchange=" + exchange +
                ", tradingPair=" + tradingPair +
                ", monitor=" + monitor +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MonitorConfigId that = (MonitorConfigId) o;
        return userId == that.userId && exchange == that.exchange && tradingPair == that.tradingPair && monitor == that.monitor;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, exchange, tradingPair, monitor);
    }
}
