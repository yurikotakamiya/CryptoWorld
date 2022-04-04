package cw.trader.event;

import cw.common.config.StrategyType;
import cw.common.db.mysql.ApiKey;
import cw.common.db.mysql.StrategyConfig;
import cw.common.event.IEvent;
import cw.common.event.IEventHandler;
import cw.common.md.ChronicleUtil;
import cw.common.md.Exchange;
import cw.common.md.TradingPair;
import cw.common.timer.ITimeManager;
import cw.common.timer.Timer;
import cw.trader.ExchangeApiHandler;
import cw.trader.OrderResponse;
import cw.trader.strategy.AbstractTraderStrategy;
import cw.trader.strategy.interval.TraderIntervalStrategy;
import cwp.db.mysql.MySqlAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class TraderEventHandler implements IEventHandler {
    private static final Logger LOGGER = LogManager.getLogger(TraderEventHandler.class.getSimpleName());

    private final Map<TradingPair, Map<Exchange, Map<StrategyType, AbstractTraderStrategy>>> strategies;
    private final Map<Integer, AbstractTraderStrategy> strategiesById;
    private final Map<Exchange, ExchangeApiHandler> exchangeApiHandlers;
    private final MySqlAdapter dbAdapter;
    private final ITimeManager timeManager;
    private final Consumer<Timer> timerConsumer;

    public TraderEventHandler(Map<Exchange, ExchangeApiHandler> exchangeApiHandlers, MySqlAdapter dbAdapter, ITimeManager timeManager, Consumer<Timer> timerConsumer) {
        this.strategies = new HashMap<>();
        this.strategiesById = new HashMap<>();
        this.exchangeApiHandlers = exchangeApiHandlers;
        this.dbAdapter = dbAdapter;
        this.timeManager = timeManager;
        this.timerConsumer = timerConsumer;
    }

    @Override
    public void process(IEvent event) throws Exception {
        if (event instanceof Timer) {
            handleTimer((Timer) event);
        } else if (event instanceof OrderResponse) {
            handleOrderResponse((OrderResponse) event);
        } else if (event instanceof ApiKey) {
            handleApiKey((ApiKey) event);
        } else if (event instanceof StrategyConfig) {
            handleStrategyConfig((StrategyConfig) event);
        } else {
            LOGGER.error("Received unknown event {}.", event);
        }
    }

    private void handleTimer(Timer timer) throws Exception {
        AbstractTraderStrategy strategy = this.strategiesById.get(timer.consumerId);

        if (strategy != null) {
            strategy.onTimerEvent(timer);
        } else {
            LOGGER.error("No strategy found for {}.", timer);
        }
    }

    private void handleOrderResponse(OrderResponse event) {
        AbstractTraderStrategy strategy = strategiesById.get(event.strategyId);

        if (strategy == null) {
            LOGGER.error("No strategy found for {}.", event);
        }

        strategy.onOrderResponse(event);
    }

    private void handleApiKey(ApiKey apiKey) {
        Exchange exchange = Exchange.values()[apiKey.getExchange()];
        ExchangeApiHandler apiHandler = this.exchangeApiHandlers.get(exchange);

        if (apiHandler != null) {
            apiHandler.add(apiKey);
        }
    }

    private void handleStrategyConfig(StrategyConfig strategyConfig) throws Exception {
        Exchange exchange = Exchange.values()[strategyConfig.getExchange()];
        TradingPair tradingPair = TradingPair.values()[strategyConfig.getTradingPair()];
        StrategyType strategy = StrategyType.values()[strategyConfig.getStrategy()];

        Map<Exchange, Map<StrategyType, AbstractTraderStrategy>> strategiesByExchange = this.strategies.computeIfAbsent(tradingPair, p -> new HashMap<>());
        Map<StrategyType, AbstractTraderStrategy> strategiesByType = strategiesByExchange.computeIfAbsent(exchange, e -> new HashMap<>());
        AbstractTraderStrategy traderStrategy = strategiesByType.get(strategy);

        if (traderStrategy == null) {
            if (strategy == StrategyType.INTERVAL) {
                traderStrategy = new TraderIntervalStrategy(ChronicleUtil.getQuoteMap(exchange, tradingPair), ChronicleUtil.getQuote(), this.dbAdapter, this.timeManager, this.timerConsumer, exchange, this.exchangeApiHandlers.get(exchange), tradingPair);
            }

            strategiesByType.put(strategy, traderStrategy);
            this.strategiesById.put(traderStrategy.getId(), traderStrategy);

            LOGGER.info("Added new strategy {} {} {}.", exchange, tradingPair, strategy);
        }

        traderStrategy.onStrategyConfig(strategyConfig);
    }
}
