package cw.trader.strategy.interval;

import cw.common.db.mysql.StrategyType;
import cw.common.db.mysql.Order;
import cw.common.db.mysql.StrategyConfig;
import cw.common.db.mysql.OrderTimeInForce;
import cw.common.db.mysql.Trade;
import cw.common.id.IdGenerator;
import cw.common.db.mysql.Exchange;
import cw.common.md.MarketDataType;
import cw.common.md.Quote;
import cw.common.db.mysql.TradingPair;
import cw.common.db.mysql.OrderAction;
import cw.common.db.mysql.OrderSide;
import cw.common.db.mysql.OrderState;
import cw.common.db.mysql.OrderType;
import cw.common.timer.ITimeManager;
import cw.common.timer.TimeManager;
import cw.common.timer.Timer;
import cw.common.timer.TimerType;
import cw.trader.ExchangeApiHandler;
import cw.trader.OrderResponse;
import cw.trader.strategy.AbstractTraderStrategy;
import cwp.db.IDbAdapter;
import edu.emory.mathcs.backport.java.util.Collections;
import net.openhft.chronicle.map.ChronicleMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.decimal4j.util.DoubleRounder;
import org.jetbrains.annotations.TestOnly;

import java.util.*;
import java.util.function.Consumer;

public class TraderIntervalStrategy extends AbstractTraderStrategy {
    private static final Logger LOGGER = LogManager.getLogger(TraderIntervalStrategy.class.getSimpleName());
    private static final long QUOTE_INTERVAL = 500;
    private static final long HEALTH_CHECK_INTERVAL = 5 * TimeManager.ONE_SEC;

    private final ExchangeApiHandler apiHandler;

    private final Map<Integer, StrategyConfig> strategyConfigs;
    private final Map<Integer, String> bidSizes;
    private final Map<Double, Map<Integer, Double>> askSizes;

    private final TreeMap<Double, Set<Integer>> buyOrders;
    private final TreeMap<Double, Set<Integer>> sellOrders;
    private final Map<Long, Order> openOrders;
    private final Map<Integer, Set<Long>> pendingOrdersByUser;
    private final Map<Integer, Set<Double>> boughtPrices;
    private final Map<Integer, Map<Double, Double>> askToBidPrice;

    public TraderIntervalStrategy(ChronicleMap<TradingPair, Quote> chronicleMap, Quote quote, IDbAdapter dbAdapter, ITimeManager timeManager, Consumer<Timer> timerScheduler, Exchange exchange, ExchangeApiHandler apiHandler, TradingPair tradingPair) throws Exception {
        super(chronicleMap, quote, dbAdapter, timeManager, timerScheduler, exchange, tradingPair);

        this.apiHandler = apiHandler;

        this.strategyConfigs = new HashMap<>();
        this.bidSizes = new HashMap<>();
        this.askSizes = new HashMap<>();

        this.buyOrders = new TreeMap<>(Comparator.reverseOrder());
        this.sellOrders = new TreeMap<>();
        this.openOrders = new HashMap<>();
        this.pendingOrdersByUser = new HashMap<>();
        this.boughtPrices = new HashMap<>();
        this.askToBidPrice = new HashMap<>();

        validate();
        scheduleTimers();
    }

    private void validate() throws Exception {
        if (this.apiHandler == null) throw new Exception("No handler has been created.");
    }

    private void scheduleTimers() {
        if (getInterestedMarketDataTypes().contains(MarketDataType.QUOTE)) {
            long expirationTime = this.timeManager.getCurrentTimeMillis() + QUOTE_INTERVAL;
            scheduleTimer(new Timer(this.id, TimerType.QUOTE, expirationTime, exchange, tradingPair));

            LOGGER.info("Scheduling {} timer with {} delay for {}.", TimerType.QUOTE, QUOTE_INTERVAL, getStrategyType());
        }

        long expirationTime = this.timeManager.getCurrentTimeMillis() + HEALTH_CHECK_INTERVAL;
        scheduleTimer(new Timer(this.id, TimerType.HEALTH_CHECK, expirationTime, exchange, tradingPair));

        LOGGER.info("Scheduling {} timer with {} delay for {}.", TimerType.HEALTH_CHECK, HEALTH_CHECK_INTERVAL, getStrategyType());
    }

    @Override
    public Exchange getExchange() {
        return this.exchange;
    }

    @Override
    public TradingPair getTradingPair() {
        return this.tradingPair;
    }

    @Override
    public StrategyType getStrategyType() {
        return StrategyType.INTERVAL;
    }

    @Override
    public void onTimerEvent(Timer timer) throws Exception {
        if (timer.timerType == TimerType.QUOTE) {
            this.chronicleMap.getUsing(this.tradingPair, this.quote);

            double bidPrice = this.quote.getBidPrice();
            double askPrice = this.quote.getAskPrice();

            for (Map.Entry<Double, Set<Integer>> entry : this.sellOrders.entrySet()) {
                double sellPrice = entry.getKey();
                if (sellPrice > bidPrice) break;

                for (int userId : entry.getValue()) {
                    entry.getValue().remove(userId);
                    Double askSize = this.askSizes.get(sellPrice).get(userId);

                    if (askSize == null) {
                        LOGGER.error("Could not find askSize for {}.", userId);
                        continue;
                    }

                    String orderSize = String.valueOf(askSize);
                    String orderPrice = String.valueOf(sellPrice);
                    Date now = new Date();

                    long orderId = IdGenerator.nextOrderTradeId();
                    Order order = new Order();
                    order.setId(orderId);
                    order.setUserId(userId);
                    order.setOrderAction((byte) OrderAction.SUBMIT.ordinal());
                    order.setOrderState((byte) OrderState.SUBMIT.ordinal());
                    order.setCreateTime(now);
                    order.setUpdateTime(now);
                    order.setTradingPair((byte) getTradingPair().ordinal());
                    order.setStrategy((byte) getStrategyType().ordinal());
                    order.setOrderTimeInForce((byte) OrderTimeInForce.FOK.ordinal());
                    order.setOrderSide((byte) OrderSide.SELL.ordinal());
                    order.setOrderType((byte) OrderType.LIMIT.ordinal());
                    order.setOrderPrice(sellPrice);
                    order.setOrderSize(askSize);
                    order.setLeavesQuantity(askSize);
                    order.setVersion(1);

                    this.openOrders.put(orderId, order);
                    this.pendingOrdersByUser.computeIfAbsent(userId, u -> new HashSet<>()).add(orderId);
                    this.dbAdapter.write(order);

                    LOGGER.info("Sending sell order price {} and quantity {} for user {}.", orderPrice, orderSize, userId);

                    this.apiHandler.submitLimitFok(this, userId, orderId, orderSize, orderPrice, sellPrice, OrderSide.SELL);
                }
            }

            for (Map.Entry<Double, Set<Integer>> entry : this.buyOrders.entrySet()) {
                double buyPrice = entry.getKey();
                if (buyPrice < askPrice) break;

                String orderPrice = String.valueOf(buyPrice);
                Set<Integer> userIds = entry.getValue();

                Iterator<Integer> iterator = userIds.iterator();

                while (iterator.hasNext()) {
                    int userId = iterator.next();
                    iterator.remove();

                    String orderSize = this.bidSizes.get(userId);
                    double buySize = this.strategyConfigs.get(userId).getParamIntervalOrderSize();
                    Date now = new Date();

                    long orderId = IdGenerator.nextOrderTradeId();
                    Order order = new Order();
                    order.setId(orderId);
                    order.setUserId(userId);
                    order.setOrderAction((byte) OrderAction.SUBMIT.ordinal());
                    order.setOrderState((byte) OrderState.SUBMIT.ordinal());
                    order.setCreateTime(now);
                    order.setUpdateTime(now);
                    order.setTradingPair((byte) getTradingPair().ordinal());
                    order.setStrategy((byte) getStrategyType().ordinal());
                    order.setOrderTimeInForce((byte) OrderTimeInForce.FOK.ordinal());
                    order.setOrderSide((byte) OrderSide.BUY.ordinal());
                    order.setOrderType((byte) OrderType.LIMIT.ordinal());
                    order.setOrderPrice(buyPrice);
                    order.setOrderSize(buySize);
                    order.setLeavesQuantity(buySize);
                    order.setVersion(1);

                    this.openOrders.put(orderId, order);
                    this.pendingOrdersByUser.computeIfAbsent(userId, u -> new HashSet<>()).add(orderId);
                    this.dbAdapter.write(order);

                    LOGGER.info("Sending buy order price {} and quantity {} for user {}.", orderPrice, orderSize, userId);

                    this.apiHandler.submitLimitFok(this, userId, orderId, orderSize, orderPrice, buyPrice, OrderSide.BUY);
                }
            }

            timer.expirationTime = this.timeManager.getCurrentTimeMillis() + QUOTE_INTERVAL;
            scheduleTimer(timer);
        } else if (timer.timerType == TimerType.HEALTH_CHECK) {
            for (Map.Entry<Integer, Set<Long>> entry : this.pendingOrdersByUser.entrySet()) {
                if (entry.getValue().isEmpty()) {
                    int userId = entry.getKey();

                    StrategyConfig strategyConfig = this.strategyConfigs.get(userId);
                    if (strategyConfig == null) {
                        LOGGER.error("No strategy config found during health check.");
                        return;
                    }

                    double startPrice = strategyConfig.getParamIntervalStartPrice();
                    double interval = strategyConfig.getParamIntervalPriceInterval();
                    double quoteAskPrice = this.quote.getAskPrice();
                    double newBidPrice = 0;

                    if (startPrice > quoteAskPrice) {
                        newBidPrice = startPrice - (int) ((startPrice - quoteAskPrice) / interval) * interval;
                    } else if (startPrice < quoteAskPrice) {
                        newBidPrice = startPrice + (int) ((quoteAskPrice - startPrice) / interval + 1) * interval;
                    }

                    newBidPrice = DoubleRounder.round(newBidPrice, 5);

                    if (newBidPrice != 0 && !this.boughtPrices.get(userId).contains(newBidPrice)) {
                        this.buyOrders.computeIfAbsent(newBidPrice, n -> new HashSet<>()).add(userId);
                    }
                }
            }

            timer.expirationTime = this.timeManager.getCurrentTimeMillis() + HEALTH_CHECK_INTERVAL;
            scheduleTimer(timer);
        }
    }

    @Override
    public void onStrategyConfig(StrategyConfig strategyConfig) {
        if (Exchange.values()[strategyConfig.getExchange()] != this.exchange) return;
        if (TradingPair.values()[strategyConfig.getTradingPair()] != this.tradingPair) return;
        if (StrategyType.values()[strategyConfig.getStrategy()] != StrategyType.INTERVAL) return;

        if (strategyConfig.getParamIntervalStartPrice() == null || strategyConfig.getParamIntervalStartPrice() < 0) {
            LOGGER.warn("Ignoring {} due to invalid start price config.", strategyConfig);
            return;
        }

        if (strategyConfig.getParamIntervalPriceInterval() == null || strategyConfig.getParamIntervalPriceInterval() < 0) {
            LOGGER.warn("Ignoring {} due to invalid price interval config.", strategyConfig);
            return;
        }

        if (strategyConfig.getParamIntervalProfitPriceChange() == null || strategyConfig.getParamIntervalProfitPriceChange() < 0) {
            LOGGER.warn("Ignoring {} due to invalid profit price change config.", strategyConfig);
            return;
        }

        if (strategyConfig.getParamIntervalOrderSize() == null || strategyConfig.getParamIntervalOrderSize() < 0) {
            LOGGER.warn("Ignoring {} due to invalid order size config.", strategyConfig);
            return;
        }

        if (!this.apiHandler.validate(strategyConfig)) {
            LOGGER.warn("Ignoring {} due to invalid config specific to exchange={}.", strategyConfig, exchange);
            return;
        }

        int userId = strategyConfig.getUserId();
        this.strategyConfigs.put(userId, strategyConfig);
        this.bidSizes.put(userId, String.valueOf(strategyConfig.getParamIntervalOrderSize()));

        LOGGER.info("Updated {} for user {}.", strategyConfig, userId);

        double orderPrice = strategyConfig.getParamIntervalStartPrice();
        Set<Double> boughtPricesForUser = this.boughtPrices.computeIfAbsent(userId, u -> new HashSet<>());

        if (!boughtPricesForUser.contains(orderPrice)) {
            this.buyOrders.computeIfAbsent(orderPrice, b -> new HashSet<>()).add(userId);

            LOGGER.info("Preparing a buy order for user {} at configured start price {}.", userId, orderPrice);
        }
    }

    @Override
    public void onOrderResponse(OrderResponse orderResponse) {
        long orderId = orderResponse.orderId;
        Order order = this.openOrders.get(orderId);

        if (OrderState.values()[order.getOrderState()] == OrderState.SUBMIT) {
            order.setClientOrderId(orderResponse.clientOrderId);

            if (orderResponse.orderState == OrderState.SUBMIT_REJECTED) {
                order.setOrderAction((byte) OrderAction.SUBMIT_REJECTED.ordinal());
                order.setOrderState((byte) OrderState.SUBMIT_REJECTED.ordinal());
            } else {
                order.setOrderAction((byte) OrderAction.SUBMITTED.ordinal());
                order.setOrderState((byte) OrderState.SUBMITTED.ordinal());
            }

            order.setVersion(order.getVersion() + 1);
            this.dbAdapter.write(order);
        }

        for (int i = 0; i < orderResponse.executedQuantities.size(); i++) {
            double quantity = orderResponse.executedQuantities.get(i);
            double price = orderResponse.executionPrice.get(i);

            if (orderResponse.orderState == OrderState.EXECUTED) {
                if (i == orderResponse.executedQuantities.size() - 1) {
                    order.setOrderAction((byte) OrderAction.EXECUTED.ordinal());
                    order.setOrderState((byte) OrderState.EXECUTED.ordinal());
                } else {
                    order.setOrderState((byte) OrderState.PARTIAL_EXEC.ordinal());
                }
            } else if (orderResponse.orderState == OrderState.PARTIAL_EXEC) {
                order.setOrderState((byte) OrderState.PARTIAL_EXEC.ordinal());
            }

            order.setLeavesQuantity(Math.max(0, DoubleRounder.round(order.getLeavesQuantity() - quantity, 5)));
            order.setExecutedQuantity(DoubleRounder.round(order.getExecutedQuantity() + quantity, 5));
            order.setUpdateTime(new Date());
            order.setVersion(order.getVersion() + 1);
            this.dbAdapter.write(order);

            Trade trade = new Trade();
            trade.setId(IdGenerator.nextOrderTradeId());
            trade.setUserId(order.getUserId());
            trade.setOrderId(orderId);
            trade.setStrategy(order.getStrategy());
            trade.setTradePrice(price);
            trade.setTradeSize(quantity);
            trade.setTradeNotional(DoubleRounder.round(price * quantity, 5));
            trade.setTradingPair(order.getTradingPair());
            trade.setTradeSide(order.getOrderSide());
            trade.setTradeType(order.getOrderType());
            trade.setTradeTimeInForce(order.getOrderTimeInForce());
            trade.setExecutionTime(order.getUpdateTime());
            this.dbAdapter.write(trade);
        }

        if (orderResponse.orderState == OrderState.CANCELED) {
            order.setOrderAction((byte) orderResponse.orderAction.ordinal());
            order.setOrderState((byte) orderResponse.orderState.ordinal());
            order.setLeavesQuantity(0);
            order.setVersion(order.getVersion() + 1);
            this.dbAdapter.write(order);
        }

        Double bidPrice = orderResponse.bidPrice;
        Double askPrice = orderResponse.askPrice;

        if (OrderState.values()[order.getOrderState()].isComplete()) {
            int userId = orderResponse.userId;

            this.orderIdToClientOrderId.remove(orderId);
            this.openOrders.remove(orderId);
            this.pendingOrdersByUser.get(userId).remove(orderId);

            if (bidPrice != null) {
                StrategyConfig strategyConfig = this.strategyConfigs.get(userId);
                double priceInterval = strategyConfig.getParamIntervalPriceInterval();
                double profitPriceChange = strategyConfig.getParamIntervalProfitPriceChange();

                if (order.getExecutedQuantity() > 0) {
                    Set<Double> boughtPricesForUser = this.boughtPrices.get(userId);
                    boughtPricesForUser.add(bidPrice);

                    double quoteAskPrice = this.quote.getAskPrice();
                    double newBidPrice = bidPrice - priceInterval;

                    if (quoteAskPrice < newBidPrice) {
                        newBidPrice = bidPrice - (int) ((bidPrice - quoteAskPrice) / priceInterval) * priceInterval;
                    }

                    newBidPrice = DoubleRounder.round(newBidPrice, 5);

                    if (!boughtPricesForUser.contains(newBidPrice)) {
                        Set<Integer> userIdsForBidPrice = this.buyOrders.computeIfAbsent(newBidPrice, n -> new HashSet<>());
                        userIdsForBidPrice.add(userId);

                        LOGGER.info("Preparing a buy order for user {} at price {}.", userId, newBidPrice);
                    }

                    double newAskPrice = DoubleRounder.round(bidPrice + profitPriceChange, 5);
                    Set<Integer> userIdsForAskPrice = this.sellOrders.computeIfAbsent(newAskPrice, n -> new HashSet<>());
                    userIdsForAskPrice.add(userId);

                    LOGGER.info("Preparing a sell order for user {} at price {} to close the fill at price {}.", userId, newAskPrice, bidPrice);

                    Map<Double, Double> askToBidPriceForUser = this.askToBidPrice.computeIfAbsent(userId, u -> new HashMap<>());
                    askToBidPriceForUser.put(newAskPrice, bidPrice);

                    Map<Integer, Double> askSizesByUser = this.askSizes.computeIfAbsent(newAskPrice, n -> new HashMap<>());
                    askSizesByUser.put(userId, order.getExecutedQuantity());
                } else {
                    this.buyOrders.get(bidPrice).add(userId);
                }
            } else if (askPrice != null) {
                if (order.getExecutedQuantity() > 0) {
                    this.askSizes.get(askPrice).remove(userId);

                    double originalBidPrice = this.askToBidPrice.get(userId).remove(askPrice);
                    this.buyOrders.get(originalBidPrice).add(userId);
                    this.boughtPrices.get(userId).remove(originalBidPrice);
                } else {
                    this.sellOrders.get(askPrice).add(userId);
                }
            } else {
                LOGGER.error("Neither bid or ask price was sent for {}.", orderResponse);
            }
        }
    }

    @Override
    public Collection<MarketDataType> getInterestedMarketDataTypes() {
        return Collections.singleton(MarketDataType.QUOTE);
    }

    @TestOnly
    public Map<Integer, StrategyConfig> getStrategyConfigs() {
        return this.strategyConfigs;
    }

    @TestOnly
    public Map<Integer, String> getBidSizes() {
        return this.bidSizes;
    }

    @TestOnly
    public Map<Double, Map<Integer, Double>> getAskSizes() {
        return this.askSizes;
    }

    @TestOnly
    public TreeMap<Double, Set<Integer>> getBuyOrders() {
        return this.buyOrders;
    }

    @TestOnly
    public TreeMap<Double, Set<Integer>> getSellOrders() {
        return this.sellOrders;
    }

    @TestOnly
    public Map<Long, Order> getOpenOrders() {
        return this.openOrders;
    }

    @TestOnly
    public Map<Integer, Set<Double>> getBoughtPrices() {
        return this.boughtPrices;
    }

    @TestOnly
    public Map<Integer, Map<Double, Double>> getAskToBidPrice() {
        return this.askToBidPrice;
    }
}
