package cw.trader.event;

import cw.common.event.IEvent;
import cw.common.event.IEventHandler;
import cw.common.md.Exchange;
import cw.common.md.MarketDataType;
import cw.common.md.TradingPair;
import cw.common.timer.ITimeManager;
import cw.common.timer.Timer;
import cw.common.timer.TimerType;
import cw.trader.strategy.ITraderStrategy;
import cw.trader.strategy.TraderStrategyType;
import cw.trader.strategy.interval.TraderIntervalStrategy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class TraderEventHandler implements IEventHandler {
    private static final Logger LOGGER = LogManager.getLogger(TraderEventHandler.class.getSimpleName());
    private static final long QUOTE_INTERVAL = 500;

    private final Map<TradingPair, Map<Exchange, Map<TraderStrategyType, ITraderStrategy>>> strategies;
    private final ITimeManager timeManager;
    private final Consumer<Timer> timerConsumer;

    public TraderEventHandler(ITimeManager timeManager, Consumer<Timer> timerConsumer) {
        this.strategies = new HashMap<>();
        this.timeManager = timeManager;
        this.timerConsumer = timerConsumer;
    }

    @Override
    public void process(IEvent event) throws Exception {
        if (event instanceof Timer) {
            handleTimer((Timer) event);
        } else if (event instanceof TraderStrategyStartRequest) {
            startStrategy((TraderStrategyStartRequest) event);
        } else {
            LOGGER.error("Received unknown event {}.", event);
        }
    }

    private void handleTimer(Timer timer) {
        if (timer.timerType == TimerType.QUOTE) {
            Map<Exchange, Map<TraderStrategyType, ITraderStrategy>> map = this.strategies.get(timer.tradingPair);
            if (map == null) return;

            Map<TraderStrategyType, ITraderStrategy> strategies = map.get(timer.exchange);
            if (strategies == null) return;

            for (ITraderStrategy strategy : strategies.values()) {
                strategy.onTimerEvent(timer);
            }

            timer.expirationTime = this.timeManager.getCurrentTimeMillis() + QUOTE_INTERVAL;
            this.timerConsumer.accept(timer);
        }
    }

    private void startStrategy(TraderStrategyStartRequest startRequest) throws Exception {
        TraderStrategyType strategyType = startRequest.strategyType;
        Exchange exchange = startRequest.exchange;
        TradingPair tradingPair = startRequest.tradingPair;

        Map<Exchange, Map<TraderStrategyType, ITraderStrategy>> strategiesByExchange = this.strategies.get(tradingPair);

        if (strategiesByExchange != null) {
            Map<TraderStrategyType, ITraderStrategy> strategiesByType = strategiesByExchange.get(exchange);

            if ((strategiesByType != null) && strategiesByType.containsKey(strategyType)) {
                LOGGER.info("Not starting new strategy for {} {} {} because it already exists.", strategyType, exchange, tradingPair);
                return;
            }
        }

        ITraderStrategy strategy = null;

        if (strategyType == TraderStrategyType.INTERVAL) {
            strategy = new TraderIntervalStrategy(startRequest.exchange, tradingPair);
        }

        if (strategy == null) {
            LOGGER.error("Unknown strategy type {}.", strategyType);
            return;
        }

        strategiesByExchange = this.strategies.computeIfAbsent(tradingPair, t -> new HashMap<>());
        strategiesByExchange.computeIfAbsent(exchange, e -> new HashMap<>()).put(strategyType, strategy);

        LOGGER.info("Added new strategy {} {} {}.", strategyType, exchange, tradingPair);

        for (MarketDataType marketDataType : strategy.getInterestedMarketDataTypes()) {
            if (marketDataType == MarketDataType.QUOTE) {
                long expirationTime = this.timeManager.getCurrentTimeMillis() + QUOTE_INTERVAL;
                this.timerConsumer.accept(new Timer(TimerType.QUOTE, expirationTime, exchange, tradingPair));

                LOGGER.info("Scheduling {} timer with {} delay for {}.", marketDataType, QUOTE_INTERVAL, strategyType);
            }
        }
    }
}
