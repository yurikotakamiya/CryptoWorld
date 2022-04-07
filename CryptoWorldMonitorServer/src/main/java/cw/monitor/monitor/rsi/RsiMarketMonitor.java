package cw.monitor.monitor.rsi;

import cw.common.db.mysql.Exchange;
import cw.common.db.mysql.MonitorConfig;
import cw.common.db.mysql.MonitorType;
import cw.common.db.mysql.TradingPair;
import cw.common.md.Quote;
import cw.common.timer.ITimeManager;
import cw.common.timer.Timer;
import cw.common.timer.TimerType;
import cw.monitor.monitor.AbstractMarketMonitor;
import cwp.db.IDbAdapter;
import net.openhft.chronicle.map.ChronicleMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Consumer;

public class RsiMarketMonitor extends AbstractMarketMonitor {
    private static final Logger LOGGER = LogManager.getLogger(RsiMarketMonitor.class.getSimpleName());
    private static final int QUOTE_INTERVAL = 500;

    private final Map<Integer, MonitorConfig> monitorConfigs;
    private final TreeMap<Double, Set<Integer>> lowThresholds;
    private final TreeMap<Double, Set<Integer>> highThresholds;

    private double currentRsi;

    public RsiMarketMonitor(ChronicleMap<TradingPair, Quote> chronicleMap, Quote quote, IDbAdapter dbAdapter, ITimeManager timeManager, Consumer<Timer> timerScheduler, Exchange exchange, TradingPair tradingPair) {
        super(chronicleMap, quote, dbAdapter, timeManager, timerScheduler, exchange, tradingPair);

        this.monitorConfigs = new HashMap<>();
        this.lowThresholds = new TreeMap<>();
        this.highThresholds = new TreeMap<>(Comparator.reverseOrder());

        scheduleQuoteTimer();
    }

    private void scheduleQuoteTimer() {
        long expirationTime = this.timeManager.getCurrentTimeMillis() + QUOTE_INTERVAL;
        scheduleTimer(new Timer(this.id, TimerType.QUOTE, expirationTime, this.exchange, this.tradingPair));

        LOGGER.info("Scheduling {} timer with {} delay for {}.", TimerType.QUOTE, QUOTE_INTERVAL, getMonitorType());
    }

    @Override
    public MonitorType getMonitorType() {
        return MonitorType.RSI;
    }

    @Override
    public void onTimerEvent(Timer timer) {
        if (timer.timerType == TimerType.QUOTE) {


            long expirationTime = this.timeManager.getCurrentTimeMillis() + QUOTE_INTERVAL;
            scheduleTimer(new Timer(this.id, TimerType.QUOTE, expirationTime, this.exchange, this.tradingPair));
        }
    }

    @Override
    public void onMonitorConfig(MonitorConfig monitorConfig) {
        if (Exchange.values()[monitorConfig.getExchange()] != this.exchange) return;
        if (TradingPair.values()[monitorConfig.getTradingPair()] != this.tradingPair) return;
        if (MonitorType.values()[monitorConfig.getMonitor()] != MonitorType.RSI) return;

        Double lowThreshold = monitorConfig.getParamRsiLowThreshold();
        Double highThreshold = monitorConfig.getParamRsiHighThreshold();

        if (lowThreshold == null || lowThreshold <= 0 || lowThreshold >= 100) {
            LOGGER.warn("Ignoring {} due to invalid low threshold config.", monitorConfig);
            return;
        }

        if (highThreshold == null || highThreshold <= 0 || highThreshold >= 100) {
            LOGGER.warn("Ignoring {} due to invalid high threshold config.", monitorConfig);
            return;
        }

        int userId = monitorConfig.getUserId();
        this.monitorConfigs.put(userId, monitorConfig);
        this.lowThresholds.computeIfAbsent(lowThreshold, l -> new HashSet<>()).add(userId);
        this.highThresholds.computeIfAbsent(highThreshold, h -> new HashSet<>()).add(userId);
    }
}
