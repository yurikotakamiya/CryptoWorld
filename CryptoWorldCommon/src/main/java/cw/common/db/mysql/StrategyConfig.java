package cw.common.db.mysql;

import javax.persistence.*;

@Entity
@Table(name = "strategy_configs")
@IdClass(StrategyConfigId.class)
public class StrategyConfig {
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
    @Column(name = "strategy_id")
    private byte strategy;

    // Interval strategy parameters
    @Column(name = "param_interval_order_size")
    private double paramIntervalOrderSize;
    @Column(name = "param_interval_price_interval")
    private double paramIntervalPriceInterval;

    public StrategyConfig() {
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

    public double getParamIntervalOrderSize() {
        return this.paramIntervalOrderSize;
    }

    public void setParamIntervalOrderSize(double paramIntervalOrderSize) {
        this.paramIntervalOrderSize = paramIntervalOrderSize;
    }

    public double getParamIntervalPriceInterval() {
        return this.paramIntervalPriceInterval;
    }

    public void setParamIntervalPriceInterval(double paramIntervalPriceInterval) {
        this.paramIntervalPriceInterval = paramIntervalPriceInterval;
    }

    @Override
    public String toString() {
        return "StrategyConfig{" +
                "userId=" + userId +
                ", exchange=" + exchange +
                ", tradingPair=" + tradingPair +
                ", strategy=" + strategy +
                '}';
    }
}
