package cw.trader;

import cw.common.event.EventQueue;
import cw.common.event.IEventHandler;
import cw.common.md.Exchange;
import cw.common.md.TradingPair;
import cw.common.timer.ITimeManager;
import cw.common.timer.RealTimeManager;
import cw.common.timer.Timer;
import cw.common.timer.TimerQueue;
import cw.trader.event.TraderEventHandler;
import cw.trader.event.TraderStrategyStartRequest;
import cw.trader.strategy.TraderStrategyType;

public class TraderServer {
    private final ITimeManager timeManager;
    private final IEventHandler eventHandler;
    private final EventQueue eventQueue;
    private final TimerQueue timerQueue;

    TraderServer() {
        this.timeManager = new RealTimeManager();
        this.eventHandler = new TraderEventHandler(this.timeManager, this::scheduleTimer);
        this.eventQueue = new EventQueue(this.eventHandler);
        this.timerQueue = new TimerQueue(this.timeManager, this.eventQueue::enqueue);
    }

    private void scheduleTimer(Timer timer) {
        this.timerQueue.scheduleTimer(timer);
    }

    private void start() {
        this.eventQueue.enqueue(new TraderStrategyStartRequest(TraderStrategyType.INTERVAL, Exchange.BINANCE, TradingPair.BTCUSDT));
        this.eventQueue.enqueue(new TraderStrategyStartRequest(TraderStrategyType.INTERVAL, Exchange.BINANCE, TradingPair.ETHUSDT));

        this.eventQueue.enqueue(new TraderStrategyStartRequest(TraderStrategyType.INTERVAL, Exchange.KUCOIN, TradingPair.BTCUSDT));
        this.eventQueue.enqueue(new TraderStrategyStartRequest(TraderStrategyType.INTERVAL, Exchange.KUCOIN, TradingPair.ETHUSDT));

        this.eventQueue.enqueue(new TraderStrategyStartRequest(TraderStrategyType.INTERVAL, Exchange.FTX, TradingPair.BTCPERP));
        this.eventQueue.enqueue(new TraderStrategyStartRequest(TraderStrategyType.INTERVAL, Exchange.FTX, TradingPair.ETHPERP));

        new Thread(this.eventQueue).start();
        new Thread(this.timerQueue).start();
    }

    public static void main(String[] args) {
        new TraderServer().start();
    }
}
