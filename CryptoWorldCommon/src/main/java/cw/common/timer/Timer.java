package cw.common.timer;

import cw.common.event.IEvent;
import cw.common.md.Exchange;
import cw.common.md.TradingPair;

public class Timer implements IEvent {
    public TimerType timerType;
    public long expirationTime;

    // Quote
    public Exchange exchange;
    public TradingPair tradingPair;

    public Timer(TimerType timerType, long expirationTime, Exchange exchange, TradingPair tradingPair) {
        this.timerType = timerType;
        this.expirationTime = expirationTime;

        this.exchange = exchange;
        this.tradingPair = tradingPair;
    }
}
