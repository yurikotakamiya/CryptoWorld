package cw.monitor.monitor.rsi;

import cw.common.core.ExchangeApiHandler;
import cw.common.db.mysql.*;
import cw.common.md.Candlestick;
import cw.common.md.ChronicleUtil;
import cw.common.timer.ITimeManager;
import cw.common.timer.TimeManager;
import cw.common.timer.Timer;
import cw.common.timer.TimerType;
import cw.monitor.monitor.AbstractMarketMonitor;
import cwp.db.IDbAdapter;
import cwp.email.EmailUtil;
import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.TObjectLongMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import gnu.trove.map.hash.TObjectLongHashMap;
import net.openhft.chronicle.map.ChronicleMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.TestOnly;

import java.util.*;
import java.util.function.Consumer;

public class RsiMarketMonitor extends AbstractMarketMonitor {
    private static final Logger LOGGER = LogManager.getLogger(RsiMarketMonitor.class.getSimpleName());
    private static final int CANDLESTICK_INTERVAL = 2_000;
    private static final int CANDLESTICK_LIMIT = 14;
    private static final long NOTIFICATION_THROTTLE = 60 * TimeManager.ONE_SEC;
    private static final StringBuilder EMAIL_SUBJECT = new StringBuilder("RSI monitor ");
    private static final int EMAIL_SUBJECT_LENGTH = EMAIL_SUBJECT.length();
    private static final String EMAIL_BODY = "";

    private final Map<Integer, User> users;
    private final Map<Integer, MonitorConfig> monitorConfigs;
    private final Set<CandlestickInterval> intervals;
    private final Map<CandlestickInterval, TreeMap<Double, Set<Integer>>> lowThresholds;
    private final Map<CandlestickInterval, TreeMap<Double, Set<Integer>>> highThresholds;
    private final TObjectLongMap<CandlestickInterval> lastOpens;
    private final TObjectDoubleMap<CandlestickInterval> lastPriceChanges;
    private final TObjectDoubleMap<CandlestickInterval> averageGains;
    private final TObjectDoubleMap<CandlestickInterval> averageLosses;
    private final Map<CandlestickInterval, Map<Integer, Long>> lastNotificationTimes;

    public RsiMarketMonitor(Candlestick candlestick, Map<Integer, User> users, IDbAdapter dbAdapter, ITimeManager timeManager, Consumer<Timer> timerScheduler, Exchange exchange, ExchangeApiHandler exchangeApiHandler, TradingPair tradingPair) {
        super(new HashMap<>(), candlestick, dbAdapter, timeManager, timerScheduler, exchange, exchangeApiHandler, tradingPair);

        this.users = users;

        this.monitorConfigs = new HashMap<>();
        this.intervals = new HashSet<>();
        this.lowThresholds = new HashMap<>();
        this.highThresholds = new HashMap<>();
        this.lastOpens = new TObjectLongHashMap<>();
        this.lastPriceChanges = new TObjectDoubleHashMap<>();
        this.averageGains = new TObjectDoubleHashMap<>();
        this.averageLosses = new TObjectDoubleHashMap<>();
        this.lastNotificationTimes = new HashMap<>();

        scheduleCandlestickTimer();
    }

    private void scheduleCandlestickTimer() {
        long expirationTime = this.timeManager.getCurrentTimeMillis() + CANDLESTICK_INTERVAL;
        scheduleTimer(new Timer(this.id, TimerType.CANDLESTICK, expirationTime, this.exchange, this.tradingPair));

        LOGGER.info("Scheduling {} timer with {} delay for {}.", TimerType.CANDLESTICK, CANDLESTICK_INTERVAL, getMonitorType());
    }

    private ChronicleMap<TradingPair, Candlestick> getCandlestickMap(CandlestickInterval interval) {
        try {
            return ChronicleUtil.getCandlestickMap(this.exchange, interval, this.tradingPair);
        } catch (Exception e) {
            LOGGER.error("Error while generating candlestick chronicle map for {} {} {}.", this.exchange, this.tradingPair, interval, e);
        }
        return null;
    }

    private void handleCandlestick(CandlestickInterval interval, long openTime, double openPrice, double closePrice, boolean isLoading) {
        if (openPrice == 0 || closePrice == 0) return;

        long lastOpen = this.lastOpens.get(interval);
        double lastPriceChange = this.lastPriceChanges.get(interval);
        double averageGain = this.averageGains.get(interval);
        double averageLoss = this.averageLosses.get(interval);

        if (lastOpen < openTime) {
            if (lastPriceChange == 0) {
                lastPriceChange = closePrice - openPrice;
                this.lastPriceChanges.put(interval, lastPriceChange);
            } else {
                averageGain = (averageGain * (CANDLESTICK_LIMIT - 1) + ((lastPriceChange > 0) ? lastPriceChange : 0)) / CANDLESTICK_LIMIT;
                averageLoss = (averageLoss * (CANDLESTICK_LIMIT - 1) + ((lastPriceChange < 0) ? lastPriceChange : 0)) / CANDLESTICK_LIMIT;

                this.averageGains.put(interval, averageGain);
                this.averageLosses.put(interval, averageLoss);
            }

            this.lastOpens.put(interval, openTime);
        }

        double priceChange = closePrice - openPrice;
        double newAverageGain = (averageGain * (CANDLESTICK_LIMIT - 1) + ((priceChange > 0) ? priceChange : 0)) / CANDLESTICK_LIMIT;
        double newAverageLoss = (averageLoss * (CANDLESTICK_LIMIT - 1) + ((priceChange < 0) ? priceChange : 0)) / CANDLESTICK_LIMIT;

        this.lastPriceChanges.put(interval, priceChange);

        if (isLoading) return;

        double rsi = calculate(newAverageGain, newAverageLoss);

        for (Map.Entry<Double, Set<Integer>> entry : this.lowThresholds.get(interval).entrySet()) {
            double threshold = entry.getKey();
            if (threshold < rsi) break;

            Set<Integer> userIds = entry.getValue();

            for (int userId : userIds) {
                User user = this.users.get(userId);

                if (user == null) {
                    LOGGER.error("Could not retrieve corresponding user for {}.", userId);
                    continue;
                }

                try {
                    Map<Integer, Long> userToLastNotificationTime = this.lastNotificationTimes.computeIfAbsent(interval, i -> new HashMap<>());
                    long lastNotificationTime = userToLastNotificationTime.computeIfAbsent(userId, u -> 0L);
                    long now = this.timeManager.getCurrentTimeMillis();

                    if (now - lastNotificationTime > NOTIFICATION_THROTTLE) {
                        EmailUtil.send(user.getEmail(), EMAIL_SUBJECT.append("below ").append(threshold).toString(), EMAIL_BODY);
                        userToLastNotificationTime.put(userId, now);

                        LOGGER.info("Sent notification to {}.", user.getId());
                    } else {
                        LOGGER.info("Throttled notification for {}.", user.getId());
                    }
                } catch (Exception e) {
                    LOGGER.error("Error occurred while sending email.", e);
                }

                EMAIL_SUBJECT.setLength(EMAIL_SUBJECT_LENGTH);
            }
        }

        for (Map.Entry<Double, Set<Integer>> entry : this.highThresholds.get(interval).entrySet()) {
            double threshold = entry.getKey();
            if (threshold > rsi) break;

            Set<Integer> userIds = entry.getValue();

            for (int userId : userIds) {
                User user = this.users.get(userId);

                if (user == null) {
                    LOGGER.error("Could not retrieve corresponding user for {}.", userId);
                    continue;
                }

                try {
                    Map<Integer, Long> userToLastNotificationTime = this.lastNotificationTimes.computeIfAbsent(interval, i -> new HashMap<>());
                    long lastNotificationTime = userToLastNotificationTime.computeIfAbsent(userId, u -> 0L);
                    long now = this.timeManager.getCurrentTimeMillis();

                    if (now - lastNotificationTime > NOTIFICATION_THROTTLE) {
                        EmailUtil.send(user.getEmail(), EMAIL_SUBJECT.append("above ").append(threshold).toString(), EMAIL_BODY);
                        userToLastNotificationTime.put(userId, now);

                        LOGGER.info("Sent notification to {}.", user.getId());
                    } else {
                        LOGGER.info("Throttled notification for {}.", user.getId());
                    }
                } catch (Exception e) {
                    LOGGER.error("Error occurred while sending email.", e);
                }

                EMAIL_SUBJECT.setLength(EMAIL_SUBJECT_LENGTH);
            }
        }
    }

    private double calculate(double averageGain, double averageLoss) {
        double relativeStrength = averageGain / -averageLoss;
        return 100 - 100 / (1 + relativeStrength);
    }

    @Override
    public MonitorType getMonitorType() {
        return MonitorType.RSI;
    }

    @Override
    public void onTimerEvent(Timer timer) {
        if (timer.timerType == TimerType.CANDLESTICK) {
            for (CandlestickInterval interval : this.intervals) {
                ChronicleMap<TradingPair, Candlestick> chronicleMap = this.chronicleMaps.computeIfAbsent(interval, this::getCandlestickMap);

                if (chronicleMap == null) {
                    LOGGER.error("Chronicle map not found for {}.", interval);
                    continue;
                }

                chronicleMap.getUsing(this.tradingPair, this.candlestick);
                long openTime = this.candlestick.getOpenTime();
                double openPrice = this.candlestick.getOpenPrice();
                double closePrice = this.candlestick.getClosePrice();

                handleCandlestick(interval, openTime, openPrice, closePrice, false);
            }

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
        CandlestickInterval interval = CandlestickInterval.values()[monitorConfig.getParamRsiTimeInterval()];

        if (lowThreshold == null || lowThreshold <= 0 || lowThreshold >= 100) {
            LOGGER.warn("Ignoring {} due to invalid low threshold config.", monitorConfig);
            return;
        }

        if (highThreshold == null || highThreshold <= 0 || highThreshold >= 100) {
            LOGGER.warn("Ignoring {} due to invalid high threshold config.", monitorConfig);
            return;
        }

        if (interval == null) {
            LOGGER.warn("Ignoring {} due to invalid time interval config.", monitorConfig);
            return;
        }

        int userId = monitorConfig.getUserId();
        this.monitorConfigs.put(userId, monitorConfig);
        this.lowThresholds.computeIfAbsent(interval, t -> new TreeMap<>()).computeIfAbsent(lowThreshold, t -> new HashSet<>()).add(userId);
        this.highThresholds.computeIfAbsent(interval, t -> new TreeMap<>(Comparator.reverseOrder())).computeIfAbsent(highThreshold, h -> new HashSet<>()).add(userId);

        if (this.intervals.add(interval)) {
            List<com.binance.api.client.domain.market.Candlestick> candlesticks = (List<com.binance.api.client.domain.market.Candlestick>) this.exchangeApiHandler.getHistoricalCandlestickBars(this.tradingPair, interval);

            double aggregateGain = 0;
            double aggregateLoss = 0;

            for (int i = 0; i < candlesticks.size(); i++) {
                com.binance.api.client.domain.market.Candlestick candlestick = candlesticks.get(i);

                if (i < CANDLESTICK_LIMIT) {
                    double openPrice = Double.parseDouble(candlestick.getOpen());
                    double closePrice = Double.parseDouble(candlestick.getClose());

                    double priceChange = closePrice - openPrice;
                    aggregateGain += (priceChange > 0) ? priceChange : 0;
                    aggregateLoss += (priceChange < 0) ? priceChange : 0;

                    this.lastOpens.put(interval, candlestick.getOpenTime());
                    this.averageGains.put(interval, aggregateGain / CANDLESTICK_LIMIT);
                    this.averageLosses.put(interval, aggregateLoss / CANDLESTICK_LIMIT);
                } else {
                    handleCandlestick(interval, candlestick.getOpenTime(), Double.parseDouble(candlestick.getOpen()), Double.parseDouble(candlestick.getClose()), true);
                }
            }
        }
    }

    @TestOnly
    public TObjectDoubleMap<CandlestickInterval> getAverageGains() {
        return this.averageGains;
    }

    @TestOnly
    public TObjectDoubleMap<CandlestickInterval> getAverageLosses() {
        return this.averageLosses;
    }
}
