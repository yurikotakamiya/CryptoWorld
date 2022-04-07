package cw.monitor.monitor.rsi;

import cw.common.db.mysql.*;
import cw.common.md.Candlestick;
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
    private static final int CANDLESTICK_INTERVAL = 5_000;

    private final Map<Integer, MonitorConfig> monitorConfigs;
    private final Map<CandlestickInterval, TreeMap<Double, Set<Integer>>> lowThresholds;
    private final Map<CandlestickInterval, TreeMap<Double, Set<Integer>>> highThresholds;

    private double currentRsi;

    public RsiMarketMonitor(ChronicleMap<TradingPair, Candlestick> chronicleMap, Candlestick candlestick, IDbAdapter dbAdapter, ITimeManager timeManager, Consumer<Timer> timerScheduler, Exchange exchange, TradingPair tradingPair) {
        super(chronicleMap, candlestick, dbAdapter, timeManager, timerScheduler, exchange, tradingPair);

        this.monitorConfigs = new HashMap<>();
        this.lowThresholds = new HashMap<>();
        this.highThresholds = new HashMap<>();

        scheduleCandlestickTimer();
    }

    private void scheduleCandlestickTimer() {
        long expirationTime = this.timeManager.getCurrentTimeMillis() + CANDLESTICK_INTERVAL;
        scheduleTimer(new Timer(this.id, TimerType.CANDLESTICK, expirationTime, this.exchange, this.tradingPair));

        LOGGER.info("Scheduling {} timer with {} delay for {}.", TimerType.CANDLESTICK, CANDLESTICK_INTERVAL, getMonitorType());
    }

    @Override
    public MonitorType getMonitorType() {
        return MonitorType.RSI;
    }

    @Override
    public void onTimerEvent(Timer timer) {
        if (timer.timerType == TimerType.CANDLESTICK) {


            long expirationTime = this.timeManager.getCurrentTimeMillis() + CANDLESTICK_INTERVAL;
            scheduleTimer(new Timer(this.id, TimerType.CANDLESTICK, expirationTime, this.exchange, this.tradingPair));
        }
    }

    @Override
    public void onMonitorConfig(MonitorConfig monitorConfig) {
        if (Exchange.values()[monitorConfig.getExchange()] != this.exchange) return;
        if (TradingPair.values()[monitorConfig.getTradingPair()] != this.tradingPair) return;
        if (MonitorType.values()[monitorConfig.getMonitor()] != MonitorType.RSI) return;

        Double lowThreshold = monitorConfig.getParamRsiLowThreshold();
        Double highThreshold = monitorConfig.getParamRsiHighThreshold();
        CandlestickInterval timeInterval = CandlestickInterval.values()[monitorConfig.getParamRsiTimeInterval()];

        if (lowThreshold == null || lowThreshold <= 0 || lowThreshold >= 100) {
            LOGGER.warn("Ignoring {} due to invalid low threshold config.", monitorConfig);
            return;
        }

        if (highThreshold == null || highThreshold <= 0 || highThreshold >= 100) {
            LOGGER.warn("Ignoring {} due to invalid high threshold config.", monitorConfig);
            return;
        }

        if (timeInterval == null) {
            LOGGER.warn("Ignoring {} due to invalid time interval config.", monitorConfig);
            return;
        }

        int userId = monitorConfig.getUserId();
        this.monitorConfigs.put(userId, monitorConfig);
        this.lowThresholds.computeIfAbsent(timeInterval, t -> new TreeMap<>()).computeIfAbsent(lowThreshold, t -> new HashSet<>()).add(userId);
        this.highThresholds.computeIfAbsent(timeInterval, t -> new TreeMap<>(Comparator.reverseOrder())).computeIfAbsent(highThreshold, h -> new HashSet<>()).add(userId);
    }
}
