package cw.common.timer;

import cw.common.event.IEvent;
import cw.common.db.mysql.Exchange;
import cw.common.db.mysql.TradingPair;

public class Timer implements IEvent {
    public int consumerId;
    public TimerType timerType;
    public long expirationTime;

    // Quote
    public Exchange exchange;
    public TradingPair tradingPair;

    public Timer(int consumerId, TimerType timerType, long expirationTime, Exchange exchange, TradingPair tradingPair) {
        this.consumerId = consumerId;
        this.timerType = timerType;
        this.expirationTime = expirationTime;

        this.exchange = exchange;
        this.tradingPair = tradingPair;
    }

    @Override
    public String toString() {
        return "Timer{" +
                "consumerId=" + consumerId +
                ", timerType=" + timerType +
                ", expirationTime=" + expirationTime +
                ", exchange=" + exchange +
                ", tradingPair=" + tradingPair +
                '}';
    }
}
