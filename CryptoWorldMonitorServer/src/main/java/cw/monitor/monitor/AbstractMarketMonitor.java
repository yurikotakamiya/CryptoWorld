package cw.monitor.monitor;

import cw.common.db.mysql.Exchange;
import cw.common.db.mysql.MonitorConfig;
import cw.common.db.mysql.MonitorType;
import cw.common.db.mysql.TradingPair;
import cw.common.id.IdGenerator;
import cw.common.md.Candlestick;
import cw.common.timer.ITimeManager;
import cw.common.timer.Timer;
import cwp.db.IDbAdapter;
import net.openhft.chronicle.map.ChronicleMap;

import java.util.function.Consumer;

public abstract class AbstractMarketMonitor {
    protected final ChronicleMap<TradingPair, Candlestick> chronicleMap;
    protected final Candlestick candlestick;
    protected final IDbAdapter dbAdapter;
    protected final ITimeManager timeManager;
    protected final Consumer<Timer> timerScheduler;
    protected final Exchange exchange;
    protected final TradingPair tradingPair;

    protected final int id;

    public AbstractMarketMonitor(ChronicleMap<TradingPair, Candlestick> chronicleMap, Candlestick candlestick, IDbAdapter dbAdapter, ITimeManager timeManager, Consumer<Timer> timerScheduler, Exchange exchange, TradingPair tradingPair) {
        this.chronicleMap = chronicleMap;
        this.candlestick = candlestick;
        this.dbAdapter = dbAdapter;
        this.timeManager = timeManager;
        this.timerScheduler = timerScheduler;
        this.exchange = exchange;
        this.tradingPair = tradingPair;

        this.id = IdGenerator.nextId();
    }

    public Exchange getExchange() {
        return this.exchange;
    }

    public TradingPair getTradingPair() {
        return this.tradingPair;
    }

    public int getId() {
        return this.id;
    }

    public void scheduleTimer(Timer timer) {
        this.timerScheduler.accept(timer);
    }

    public abstract MonitorType getMonitorType();

    public abstract void onMonitorConfig(MonitorConfig monitorConfig);

    public abstract void onTimerEvent(Timer timer);
}
