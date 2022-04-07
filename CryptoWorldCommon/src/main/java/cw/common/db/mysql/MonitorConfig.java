package cw.common.db.mysql;

import cw.common.event.IEvent;
import cwp.db.IDbEntity;

import javax.persistence.*;

@Entity
@Table(name = "monitor_configs")
@IdClass(MonitorConfigId.class)
public class MonitorConfig implements IEvent, IDbEntity {
    @Id
    @Column(name = "user_id")
    private int userId;

    @Id
    @Column(name = "exchange_id")
    private byte exchange;

    @Id
    @Column(name = "trading_pair_id")
    private byte tradingPair;

    @Id
    @Column(name = "monitor_id")
    private byte monitor;

    // RSI strategy parameters
    @Column(name = "param_rsi_low_threshold")
    private Double paramRsiLowThreshold;
    @Column(name = "param_rsi_high_threshold")
    private Double paramRsiHighThreshold;

    public MonitorConfig() {
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

    public Double getParamRsiLowThreshold() {
        return this.paramRsiLowThreshold;
    }

    public void setParamRsiLowThreshold(Double paramRsiLowThreshold) {
        this.paramRsiLowThreshold = paramRsiLowThreshold;
    }

    public Double getParamRsiHighThreshold() {
        return this.paramRsiHighThreshold;
    }

    public void setParamRsiHighThreshold(Double paramRsiHighThreshold) {
        this.paramRsiHighThreshold = paramRsiHighThreshold;
    }

    @Override
    public String toString() {
        return "MonitorConfig{" +
                "userId=" + userId +
                ", exchange=" + exchange +
                ", tradingPair=" + tradingPair +
                ", monitor=" + monitor +
                ", paramRsiLowThreshold=" + paramRsiLowThreshold +
                ", paramRsiHighThreshold=" + paramRsiHighThreshold +
                '}';
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
