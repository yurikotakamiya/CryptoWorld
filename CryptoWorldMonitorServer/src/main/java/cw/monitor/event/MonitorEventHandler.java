package cw.monitor.event;

import cw.common.core.ExchangeApiHandler;
import cw.common.db.mysql.*;
import cw.common.event.IEvent;
import cw.common.event.IEventHandler;
import cw.common.md.ChronicleUtil;
import cw.common.timer.ITimeManager;
import cw.common.timer.Timer;
import cw.monitor.monitor.AbstractMarketMonitor;
import cw.monitor.monitor.rsi.RsiMarketMonitor;
import cwp.db.mysql.MySqlAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class MonitorEventHandler implements IEventHandler {
    private static final Logger LOGGER = LogManager.getLogger(MonitorEventHandler.class.getSimpleName());

    private final Map<Exchange, ExchangeApiHandler> exchangeApiHandlers;
    private final Map<TradingPair, Map<Exchange, Map<MonitorType, AbstractMarketMonitor>>> monitors;
    private final Map<Integer, AbstractMarketMonitor> monitorsById;
    private final Map<Integer, User> users;
    private final MySqlAdapter dbAdapter;
    private final ITimeManager timeManager;
    private final Consumer<Timer> timerConsumer;

    public MonitorEventHandler(Map<Exchange, ExchangeApiHandler> exchangeApiHandlers, MySqlAdapter dbAdapter, ITimeManager timeManager, Consumer<Timer> timerConsumer) {
        this.exchangeApiHandlers = exchangeApiHandlers;
        this.monitors = new HashMap<>();
        this.monitorsById = new HashMap<>();
        this.users = new HashMap<>();
        this.dbAdapter = dbAdapter;
        this.timeManager = timeManager;
        this.timerConsumer = timerConsumer;
    }

    @Override
    public void process(IEvent event) throws Exception {
        if (event instanceof Timer) {
            handleTimer((Timer) event);
        } else if (event instanceof MonitorConfig) {
            handleMonitorConfig((MonitorConfig) event);
        } else if (event instanceof User) {
            handleUser((User) event);
        } else {
            LOGGER.error("Received unknown event {}.", event);
        }
    }

    private void handleTimer(Timer timer) {
        AbstractMarketMonitor monitor = this.monitorsById.get(timer.consumerId);

        if (monitor != null) {
            monitor.onTimerEvent(timer);
        } else {
            LOGGER.error("No strategy found for {}.", timer);
        }
    }

    private void handleMonitorConfig(MonitorConfig monitorConfig) throws Exception {
        Exchange exchange = Exchange.values()[monitorConfig.getExchange()];
        TradingPair tradingPair = TradingPair.values()[monitorConfig.getTradingPair()];
        MonitorType monitorType = MonitorType.values()[monitorConfig.getMonitor()];

        Map<Exchange, Map<MonitorType, AbstractMarketMonitor>> monitorsByExchange = this.monitors.computeIfAbsent(tradingPair, p -> new HashMap<>());
        Map<MonitorType, AbstractMarketMonitor> monitorsByType = monitorsByExchange.computeIfAbsent(exchange, e -> new HashMap<>());
        AbstractMarketMonitor marketMonitor = monitorsByType.get(monitorType);

        if (marketMonitor == null) {
            marketMonitor = new RsiMarketMonitor(ChronicleUtil.getCandlestick(), this.dbAdapter, this.timeManager, this.timerConsumer, exchange, this.exchangeApiHandlers.get(exchange), tradingPair);

            monitorsByType.put(monitorType, marketMonitor);
            this.monitorsById.put(marketMonitor.getId(), marketMonitor);

            LOGGER.info("Added new strategy {} {} {}.", exchange, tradingPair, monitorType);
        }

        marketMonitor.onMonitorConfig(monitorConfig);
    }

    private void handleUser(User user) {
        this.users.put(user.getId(), user);

        LOGGER.info("Added new {}.", user);
    }
}
