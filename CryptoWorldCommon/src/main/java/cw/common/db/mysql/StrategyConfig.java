package cw.common.db.mysql;

import cw.common.event.IEvent;
import cwp.db.IDbEntity;

import javax.persistence.*;

@Entity
@Table(name = "strategy_configs")
@IdClass(StrategyConfigId.class)
public class StrategyConfig implements IEvent, IDbEntity {
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
    @Column(name = "param_interval_start_price")
    private Double paramIntervalStartPrice;
    @Column(name = "param_interval_price_interval")
    private Double paramIntervalPriceInterval;
    @Column(name = "param_interval_profit_price_change")
    private Double paramIntervalProfitPriceChange;
    @Column(name = "param_interval_order_size")
    private Double paramIntervalOrderSize;

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

    public Double getParamIntervalOrderSize() {
        return this.paramIntervalOrderSize;
    }

    public void setParamIntervalOrderSize(Double paramIntervalOrderSize) {
        this.paramIntervalOrderSize = paramIntervalOrderSize;
    }

    public Double getParamIntervalPriceInterval() {
        return this.paramIntervalPriceInterval;
    }

    public void setParamIntervalPriceInterval(Double paramIntervalPriceInterval) {
        this.paramIntervalPriceInterval = paramIntervalPriceInterval;
    }

    public Double getParamIntervalProfitPriceChange() {
        return this.paramIntervalProfitPriceChange;
    }

    public void setParamIntervalProfitPriceChange(Double paramIntervalProfitPriceChange) {
        this.paramIntervalProfitPriceChange = paramIntervalProfitPriceChange;
    }

    public Double getParamIntervalStartPrice() {
        return this.paramIntervalStartPrice;
    }

    public void setParamIntervalStartPrice(Double paramIntervalStartPrice) {
        this.paramIntervalStartPrice = paramIntervalStartPrice;
    }

    @Override
    public String toString() {
        return "StrategyConfig{" +
                "userId=" + userId +
                ", exchange=" + exchange +
                ", tradingPair=" + tradingPair +
                ", strategy=" + strategy +
                ", paramIntervalOrderSize=" + paramIntervalOrderSize +
                ", paramIntervalPriceInterval=" + paramIntervalPriceInterval +
                ", paramIntervalProfitPriceChange=" + paramIntervalProfitPriceChange +
                ", paramIntervalStartPrice=" + paramIntervalStartPrice +
                '}';
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
