package cw.trader.strategy.interval;

import com.binance.api.client.domain.OrderStatus;
import com.binance.api.client.domain.account.NewOrderResponse;
import cw.common.db.mysql.StrategyType;
import cw.common.db.mysql.*;
import cw.common.db.mysql.Exchange;
import cw.common.md.Quote;
import cw.common.db.mysql.TradingPair;
import cw.common.db.mysql.OrderAction;
import cw.common.db.mysql.OrderSide;
import cw.common.db.mysql.OrderState;
import cw.common.db.mysql.OrderType;
import cw.common.timer.TimeManager;
import cw.common.timer.Timer;
import cw.common.timer.TimerType;
import cw.trader.DbAdapterMock;
import cw.trader.OrderInfo;
import cw.trader.handler.binance.BinanceApiHandlerMock;
import cwp.db.IDbEntity;
import net.openhft.chronicle.map.ChronicleMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.LinkedList;
import java.util.List;

public class TraderIntervalStrategyTest {
    private ChronicleMap<TradingPair, Quote> chronicleMap;
    private Quote quote;
    private DbAdapterMock dbAdapter;
    private BinanceApiHandlerMock apiHandler;
    private TraderIntervalStrategy strategy;

    private Timer quoteTimer;
    private Timer healthCheckTimer;
    private StrategyConfig strategyConfig;

    @BeforeEach
    void before() throws Exception {
        this.chronicleMap = Mockito.mock(ChronicleMap.class);
        this.quote = Mockito.mock(Quote.class);
        this.dbAdapter = new DbAdapterMock();
        this.apiHandler = new BinanceApiHandlerMock();
        this.strategy = new TraderIntervalStrategy(this.chronicleMap, this.quote, this.dbAdapter, new TimeManager(), t -> {
        }, Exchange.BINANCE, this.apiHandler, TradingPair.BTCUSDT);

        Mockito.when(this.quote.getBidPrice()).thenReturn(45000d);
        Mockito.when(this.quote.getAskPrice()).thenReturn(45002d);

        ApiKey apiKey = getApiKey();
        this.apiHandler.add(apiKey);
        this.apiHandler.setEnqueueCallback(this.strategy::onOrderResponse);

        this.quoteTimer = new Timer(this.strategy.getId(), TimerType.QUOTE, 0, Exchange.BINANCE, TradingPair.BTCUSDT);
        this.healthCheckTimer = new Timer(this.strategy.getId(), TimerType.HEALTH_CHECK, 0, Exchange.BINANCE, TradingPair.BTCUSDT);
        this.strategyConfig = getStrategyConfig();
        this.strategy.onStrategyConfig(this.strategyConfig);
        Assertions.assertNotNull(this.strategy.getStrategyConfigs().get(this.strategyConfig.getUserId()));
    }

    @AfterEach
    void after() {
        Assertions.assertTrue(this.dbAdapter.hasNoNextDbEntity());
        Assertions.assertTrue(this.apiHandler.hasNoOrder());
    }

    private ApiKey getApiKey() {
        ApiKey apiKey = new ApiKey();
        apiKey.setUserId(1);
        apiKey.setApiKey("ABC");
        apiKey.setSecretKey("DEF");
        return apiKey;
    }

    private StrategyConfig getStrategyConfig() {
        StrategyConfig strategyConfig = new StrategyConfig();
        strategyConfig.setUserId(1);
        strategyConfig.setExchange((byte) Exchange.BINANCE.ordinal());
        strategyConfig.setTradingPair((byte) TradingPair.BTCUSDT.ordinal());
        strategyConfig.setStrategy((byte) StrategyType.INTERVAL.ordinal());
        strategyConfig.setParamIntervalStartPrice(45005d);
        strategyConfig.setParamIntervalPriceInterval(15d);
        strategyConfig.setParamIntervalProfitPriceChange(10d);
        strategyConfig.setParamIntervalOrderSize(0.4);
        return strategyConfig;
    }

    private void validateNextOrder(int userId, String orderSize, double orderPrice, OrderSide orderSide) {
        OrderInfo orderInfo = this.apiHandler.nextOrder();
        Assertions.assertEquals(userId, orderInfo.userId);
        Assertions.assertEquals(orderSize, orderInfo.orderSize);
        Assertions.assertEquals(orderPrice, orderInfo.orderPrice, 0.0001);
        Assertions.assertEquals(orderSide, orderInfo.orderSide);
    }

    private Order validateNextDbOrder(String clientOrderId, OrderAction action, OrderState state, OrderSide side, double price, double size, double leavesQuantity, double executedQuantity, int version) {
        IDbEntity entity = this.dbAdapter.nextDbEntity();
        Assertions.assertTrue(entity instanceof Order);

        Order order = (Order) entity;
        Assertions.assertEquals(clientOrderId, order.getClientOrderId());
        Assertions.assertEquals(action.ordinal(), order.getOrderAction());
        Assertions.assertEquals(state.ordinal(), order.getOrderState());
        Assertions.assertEquals(side.ordinal(), order.getOrderSide());
        Assertions.assertEquals(price, order.getOrderPrice(), 0.0001);
        Assertions.assertEquals(size, order.getOrderSize(), 0.0001);
        Assertions.assertEquals(leavesQuantity, order.getLeavesQuantity(), 0.0001);
        Assertions.assertEquals(executedQuantity, order.getExecutedQuantity(), 0.0001);
        Assertions.assertEquals(version, order.getVersion());

        Assertions.assertEquals(1, order.getUserId());
        Assertions.assertEquals(StrategyType.INTERVAL.ordinal(), order.getStrategy());
        Assertions.assertEquals(TradingPair.BTCUSDT.ordinal(), order.getTradingPair());
        Assertions.assertEquals(OrderType.LIMIT.ordinal(), order.getOrderType());
        Assertions.assertEquals(OrderTimeInForce.FOK.ordinal(), order.getOrderTimeInForce());

        return order;
    }

    private Trade validateNextDbTrade(long orderId, OrderSide side, double price, double size) {
        IDbEntity entity = this.dbAdapter.nextDbEntity();
        Assertions.assertTrue(entity instanceof Trade);

        Trade trade = (Trade) entity;
        Assertions.assertEquals(orderId, trade.getOrderId());
        Assertions.assertEquals(side.ordinal(), trade.getTradeSide());
        Assertions.assertEquals(price, trade.getTradePrice(), 0.0001);
        Assertions.assertEquals(size, trade.getTradeSize(), 0.0001);

        Assertions.assertEquals(1, trade.getUserId());
        Assertions.assertEquals(StrategyType.INTERVAL.ordinal(), trade.getStrategy());
        Assertions.assertEquals(TradingPair.BTCUSDT.ordinal(), trade.getTradingPair());

        return trade;
    }

    private void prepareNewOrderResponse(String clientOrderId, OrderStatus orderStatus, List<com.binance.api.client.domain.account.Trade> fills) {
        NewOrderResponse newOrderResponse = new NewOrderResponse();
        newOrderResponse.setClientOrderId(clientOrderId);
        newOrderResponse.setStatus(orderStatus);
        newOrderResponse.setTransactTime(Long.valueOf(0));
        newOrderResponse.setFills(fills);

        this.apiHandler.prepareNewOrderResponse(newOrderResponse);
    }

    private List<com.binance.api.client.domain.account.Trade> prepareTrade(List<String> quantities, List<String> prices) {
        List<com.binance.api.client.domain.account.Trade> trades = new LinkedList<>();

        for (int i = 0; i < quantities.size(); i++) {
            com.binance.api.client.domain.account.Trade trade = new com.binance.api.client.domain.account.Trade();
            trade.setQty(quantities.get(i));
            trade.setPrice(prices.get(i));
            trades.add(trade);
        }

        return trades;
    }

    @Test
    void test_BuyAndSell_SingleFilledResponse() throws Exception {
        // Assert data structures
        Assertions.assertTrue(this.strategy.getBuyOrders().get(45005d).contains(1));
        Assertions.assertTrue(this.strategy.getSellOrders().isEmpty());
        Assertions.assertEquals("0.4", this.strategy.getBidSizes().get(1));
        Assertions.assertTrue(this.strategy.getAskSizes().isEmpty());
        Assertions.assertTrue(this.strategy.getBoughtPrices().get(1).isEmpty());
        Assertions.assertTrue(this.strategy.getAskToBidPrice().isEmpty());

        // Ask price = 45002 and order price = 45005
        prepareNewOrderResponse("1", OrderStatus.FILLED, prepareTrade(List.of("0.2", "0.1", "0.1"), List.of("45002", "45004", "45005")));
        this.strategy.onTimerEvent(this.quoteTimer);
        this.apiHandler.flushNewOrderResponse(1);
        validateNextOrder(1, "0.4", 45005, OrderSide.BUY);

        // DB insertions
        Order order = validateNextDbOrder(null, OrderAction.SUBMIT, OrderState.SUBMIT, OrderSide.BUY, 45005, 0.4, 0.4, 0, 1);
        validateNextDbOrder("1", OrderAction.SUBMITTED, OrderState.SUBMITTED, OrderSide.BUY, 45005, 0.4, 0.4, 0, 2);
        validateNextDbOrder("1", OrderAction.SUBMITTED, OrderState.PARTIAL_EXEC, OrderSide.BUY, 45005, 0.4, 0.2, 0.2, 3);
        validateNextDbTrade(order.getId(), OrderSide.BUY, 45002, 0.2);
        validateNextDbOrder("1", OrderAction.SUBMITTED, OrderState.PARTIAL_EXEC, OrderSide.BUY, 45005, 0.4, 0.1, 0.3, 4);
        validateNextDbTrade(order.getId(), OrderSide.BUY, 45004, 0.1);
        validateNextDbOrder("1", OrderAction.EXECUTED, OrderState.EXECUTED, OrderSide.BUY, 45005, 0.4, 0, 0.4, 5);
        validateNextDbTrade(order.getId(), OrderSide.BUY, 45005, 0.1);

        // Assert data structures
        Assertions.assertTrue(this.strategy.getBuyOrders().get(45005d).isEmpty());
        Assertions.assertTrue(this.strategy.getSellOrders().get(45015d).contains(1));
        Assertions.assertEquals("0.4", this.strategy.getBidSizes().get(1));
        Assertions.assertEquals(0.4, this.strategy.getAskSizes().get(45015d).get(1));
        Assertions.assertTrue(this.strategy.getBoughtPrices().get(1).contains(45005d));
        Assertions.assertEquals(45005d, this.strategy.getAskToBidPrice().get(1).get(45015d));

        // No order should go out next
        this.strategy.onTimerEvent(this.quoteTimer);
        this.apiHandler.flushNewOrderResponse(1);
        this.apiHandler.hasNoOrder();
        this.dbAdapter.hasNoNextDbEntity();

        // Assert data structures
        Assertions.assertTrue(this.strategy.getBuyOrders().get(45005d).isEmpty());
        Assertions.assertTrue(this.strategy.getBuyOrders().get(44990d).contains(1));
        Assertions.assertTrue(this.strategy.getSellOrders().get(45015d).contains(1));
        Assertions.assertEquals("0.4", this.strategy.getBidSizes().get(1));
        Assertions.assertEquals(0.4, this.strategy.getAskSizes().get(45015d).get(1));
        Assertions.assertTrue(this.strategy.getBoughtPrices().get(1).contains(45005d));
        Assertions.assertEquals(45005d, this.strategy.getAskToBidPrice().get(1).get(45015d));

        // Quote updated to issue sell order
        Mockito.when(this.quote.getBidPrice()).thenReturn(45016d);
        Mockito.when(this.quote.getAskPrice()).thenReturn(45017d);

        // Bid price = 45016 and order price = 45015
        prepareNewOrderResponse("2", OrderStatus.FILLED, prepareTrade(List.of("0.15", "0.25"), List.of("45016", "45015")));
        this.strategy.onTimerEvent(this.quoteTimer);
        this.apiHandler.flushNewOrderResponse(1);
        validateNextOrder(1, "0.4", 45015, OrderSide.SELL);

        // DB insertions
        order = validateNextDbOrder(null, OrderAction.SUBMIT, OrderState.SUBMIT, OrderSide.SELL, 45015, 0.4, 0.4, 0, 1);
        validateNextDbOrder("2", OrderAction.SUBMITTED, OrderState.SUBMITTED, OrderSide.SELL, 45015, 0.4, 0.4, 0, 2);
        validateNextDbOrder("2", OrderAction.SUBMITTED, OrderState.PARTIAL_EXEC, OrderSide.SELL, 45015, 0.4, 0.25, 0.15, 3);
        validateNextDbTrade(order.getId(), OrderSide.SELL, 45016, 0.15);
        validateNextDbOrder("2", OrderAction.EXECUTED, OrderState.EXECUTED, OrderSide.SELL, 45015, 0.4, 0, 0.4, 4);
        validateNextDbTrade(order.getId(), OrderSide.SELL, 45015, 0.25);

        // Assert data structures
        Assertions.assertTrue(this.strategy.getBuyOrders().get(45005d).contains(1));
        Assertions.assertTrue(this.strategy.getBuyOrders().get(44990d).contains(1));
        Assertions.assertTrue(this.strategy.getSellOrders().get(45015d).isEmpty());
        Assertions.assertEquals("0.4", this.strategy.getBidSizes().get(1));
        Assertions.assertTrue(this.strategy.getAskSizes().get(45015d).isEmpty());
        Assertions.assertTrue(this.strategy.getBoughtPrices().get(1).isEmpty());
        Assertions.assertTrue(this.strategy.getAskToBidPrice().get(1).isEmpty());
    }

    @Test
    void test_BuyAndSell_MultipleFilledResponse() throws Exception {
        // Assert data structures
        Assertions.assertTrue(this.strategy.getBuyOrders().get(45005d).contains(1));
        Assertions.assertTrue(this.strategy.getSellOrders().isEmpty());
        Assertions.assertEquals("0.4", this.strategy.getBidSizes().get(1));
        Assertions.assertTrue(this.strategy.getAskSizes().isEmpty());
        Assertions.assertTrue(this.strategy.getBoughtPrices().get(1).isEmpty());
        Assertions.assertTrue(this.strategy.getAskToBidPrice().isEmpty());

        // Ask price = 45002 and order price = 45005
        prepareNewOrderResponse("1", OrderStatus.NEW, null);
        this.strategy.onTimerEvent(this.quoteTimer);
        this.apiHandler.flushNewOrderResponse(1);
        validateNextOrder(1, "0.4", 45005, OrderSide.BUY);

        // DB insertions
        Order order = validateNextDbOrder(null, OrderAction.SUBMIT, OrderState.SUBMIT, OrderSide.BUY, 45005, 0.4, 0.4, 0, 1);
        validateNextDbOrder("1", OrderAction.SUBMITTED, OrderState.SUBMITTED, OrderSide.BUY, 45005, 0.4, 0.4, 0, 2);

        // Nothing happens
        this.strategy.onTimerEvent(this.quoteTimer);
        this.apiHandler.flushNewOrderResponse(1);
        this.apiHandler.hasNoOrder();
        this.dbAdapter.hasNoNextDbEntity();

        // Assert data structures
        Assertions.assertTrue(this.strategy.getBuyOrders().get(45005d).isEmpty());
        Assertions.assertTrue(this.strategy.getSellOrders().isEmpty());
        Assertions.assertEquals("0.4", this.strategy.getBidSizes().get(1));
        Assertions.assertTrue(this.strategy.getAskSizes().isEmpty());
        Assertions.assertTrue(this.strategy.getBoughtPrices().get(1).isEmpty());
        Assertions.assertTrue(this.strategy.getAskToBidPrice().isEmpty());

        // Second response received
        prepareNewOrderResponse("1", OrderStatus.PARTIALLY_FILLED, prepareTrade(List.of("0.1", "0.2"), List.of("45002", "45004")));
        this.apiHandler.flushNewOrderResponse(1);

        // DB insertions
        validateNextDbOrder("1", OrderAction.SUBMITTED, OrderState.PARTIAL_EXEC, OrderSide.BUY, 45005, 0.4, 0.3, 0.1, 3);
        validateNextDbTrade(order.getId(), OrderSide.BUY, 45002, 0.1);
        validateNextDbOrder("1", OrderAction.SUBMITTED, OrderState.PARTIAL_EXEC, OrderSide.BUY, 45005, 0.4, 0.1, 0.3, 4);
        validateNextDbTrade(order.getId(), OrderSide.BUY, 45004, 0.2);

        // Assert data structures
        Assertions.assertTrue(this.strategy.getBuyOrders().get(45005d).isEmpty());
        Assertions.assertTrue(this.strategy.getSellOrders().isEmpty());
        Assertions.assertEquals("0.4", this.strategy.getBidSizes().get(1));
        Assertions.assertTrue(this.strategy.getAskSizes().isEmpty());
        Assertions.assertTrue(this.strategy.getBoughtPrices().get(1).isEmpty());
        Assertions.assertTrue(this.strategy.getAskToBidPrice().isEmpty());

        // Nothing happens
        this.strategy.onTimerEvent(this.quoteTimer);
        this.apiHandler.flushNewOrderResponse(1);
        this.apiHandler.hasNoOrder();
        this.dbAdapter.hasNoNextDbEntity();

        // Final response received
        prepareNewOrderResponse("1", OrderStatus.FILLED, prepareTrade(List.of("0.1"), List.of("45005")));
        this.apiHandler.flushNewOrderResponse(1);

        // DB insertions
        validateNextDbOrder("1", OrderAction.EXECUTED, OrderState.EXECUTED, OrderSide.BUY, 45005, 0.4, 0, 0.4, 5);
        validateNextDbTrade(order.getId(), OrderSide.BUY, 45005, 0.1);

        // Assert data structures
        Assertions.assertTrue(this.strategy.getBuyOrders().get(45005d).isEmpty());
        Assertions.assertTrue(this.strategy.getBuyOrders().get(44990d).contains(1));
        Assertions.assertTrue(this.strategy.getSellOrders().get(45015d).contains(1));
        Assertions.assertEquals("0.4", this.strategy.getBidSizes().get(1));
        Assertions.assertEquals(0.4, this.strategy.getAskSizes().get(45015d).get(1), 0.0001);
        Assertions.assertTrue(this.strategy.getBoughtPrices().get(1).contains(45005d));
        Assertions.assertEquals(45005d, this.strategy.getAskToBidPrice().get(1).get(45015d));

        // Quote updated to issue sell order
        Mockito.when(this.quote.getBidPrice()).thenReturn(45016d);
        Mockito.when(this.quote.getAskPrice()).thenReturn(45017d);

        // Bid price = 45016 and order price = 45015
        prepareNewOrderResponse("2", OrderStatus.NEW, null);
        this.strategy.onTimerEvent(this.quoteTimer);
        this.apiHandler.flushNewOrderResponse(1);
        validateNextOrder(1, "0.4", 45015, OrderSide.SELL);

        // DB insertions
        order = validateNextDbOrder(null, OrderAction.SUBMIT, OrderState.SUBMIT, OrderSide.SELL, 45015, 0.4, 0.4, 0, 1);
        validateNextDbOrder("2", OrderAction.SUBMITTED, OrderState.SUBMITTED, OrderSide.SELL, 45015, 0.4, 0.4, 0, 2);

        // Nothing happens
        this.strategy.onTimerEvent(this.quoteTimer);
        this.apiHandler.flushNewOrderResponse(1);
        this.apiHandler.hasNoOrder();
        this.dbAdapter.hasNoNextDbEntity();

        // Second response received
        prepareNewOrderResponse("2", OrderStatus.PARTIALLY_FILLED, prepareTrade(List.of("0.1", "0.2"), List.of("45016", "45015")));
        this.apiHandler.flushNewOrderResponse(1);

        // DB insertions
        validateNextDbOrder("2", OrderAction.SUBMITTED, OrderState.PARTIAL_EXEC, OrderSide.SELL, 45015, 0.4, 0.3, 0.1, 3);
        validateNextDbTrade(order.getId(), OrderSide.SELL, 45016, 0.1);
        validateNextDbOrder("2", OrderAction.SUBMITTED, OrderState.PARTIAL_EXEC, OrderSide.SELL, 45015, 0.4, 0.1, 0.3, 4);
        validateNextDbTrade(order.getId(), OrderSide.SELL, 45015, 0.2);

        // Assert data structures
        Assertions.assertTrue(this.strategy.getBuyOrders().get(45005d).isEmpty());
        Assertions.assertTrue(this.strategy.getBuyOrders().get(44990d).contains(1));
        Assertions.assertTrue(this.strategy.getSellOrders().get(45015d).isEmpty());
        Assertions.assertEquals("0.4", this.strategy.getBidSizes().get(1));
        Assertions.assertTrue(this.strategy.getAskSizes().get(45015d).containsKey(1));
        Assertions.assertTrue(this.strategy.getBoughtPrices().get(1).contains(45005d));
        Assertions.assertEquals(45005d, this.strategy.getAskToBidPrice().get(1).get(45015d));

        // Nothing happens
        this.strategy.onTimerEvent(this.quoteTimer);
        this.apiHandler.flushNewOrderResponse(1);
        this.apiHandler.hasNoOrder();
        this.dbAdapter.hasNoNextDbEntity();

        // Assert data structures
        Assertions.assertTrue(this.strategy.getBuyOrders().get(45005d).isEmpty());
        Assertions.assertTrue(this.strategy.getBuyOrders().get(44990d).contains(1));
        Assertions.assertTrue(this.strategy.getSellOrders().get(45015d).isEmpty());
        Assertions.assertEquals("0.4", this.strategy.getBidSizes().get(1));
        Assertions.assertTrue(this.strategy.getAskSizes().get(45015d).containsKey(1));
        Assertions.assertTrue(this.strategy.getBoughtPrices().get(1).contains(45005d));
        Assertions.assertEquals(45005d, this.strategy.getAskToBidPrice().get(1).get(45015d));

        // Final response received
        prepareNewOrderResponse("2", OrderStatus.FILLED, prepareTrade(List.of("0.1"), List.of("45015")));
        this.apiHandler.flushNewOrderResponse(1);

        // DB insertions
        validateNextDbOrder("2", OrderAction.EXECUTED, OrderState.EXECUTED, OrderSide.SELL, 45015, 0.4, 0, 0.4, 5);
        validateNextDbTrade(order.getId(), OrderSide.SELL, 45015, 0.1);

        // Assert data structures
        Assertions.assertTrue(this.strategy.getBuyOrders().get(45005d).contains(1));
        Assertions.assertTrue(this.strategy.getBuyOrders().get(44990d).contains(1));
        Assertions.assertTrue(this.strategy.getSellOrders().get(45015d).isEmpty());
        Assertions.assertEquals("0.4", this.strategy.getBidSizes().get(1));
        Assertions.assertTrue(this.strategy.getAskSizes().get(45015d).isEmpty());
        Assertions.assertTrue(this.strategy.getBoughtPrices().get(1).isEmpty());
        Assertions.assertTrue(this.strategy.getAskToBidPrice().get(1).isEmpty());
    }

    @Test
    void test_BuyAndSell_CanceledResponse_Buy() throws Exception {
        // Assert data structures
        Assertions.assertTrue(this.strategy.getBuyOrders().get(45005d).contains(1));
        Assertions.assertTrue(this.strategy.getSellOrders().isEmpty());
        Assertions.assertEquals("0.4", this.strategy.getBidSizes().get(1));
        Assertions.assertTrue(this.strategy.getAskSizes().isEmpty());
        Assertions.assertTrue(this.strategy.getBoughtPrices().get(1).isEmpty());
        Assertions.assertTrue(this.strategy.getAskToBidPrice().isEmpty());

        // Ask price = 45002 and order price = 45005
        prepareNewOrderResponse("1", OrderStatus.CANCELED, null);
        this.strategy.onTimerEvent(this.quoteTimer);
        this.apiHandler.flushNewOrderResponse(1);
        validateNextOrder(1, "0.4", 45005, OrderSide.BUY);

        // DB insertions
        validateNextDbOrder(null, OrderAction.SUBMIT, OrderState.SUBMIT, OrderSide.BUY, 45005, 0.4, 0.4, 0, 1);
        validateNextDbOrder("1", OrderAction.SUBMITTED, OrderState.SUBMITTED, OrderSide.BUY, 45005, 0.4, 0.4, 0, 2);
        validateNextDbOrder("1", OrderAction.CANCELED, OrderState.CANCELED, OrderSide.BUY, 45005, 0.4, 0, 0, 3);

        // Assert data structures
        Assertions.assertTrue(this.strategy.getBuyOrders().get(45005d).contains(1));
        Assertions.assertTrue(this.strategy.getSellOrders().isEmpty());
        Assertions.assertEquals("0.4", this.strategy.getBidSizes().get(1));
        Assertions.assertTrue(this.strategy.getAskSizes().isEmpty());
        Assertions.assertTrue(this.strategy.getBoughtPrices().get(1).isEmpty());
        Assertions.assertTrue(this.strategy.getAskToBidPrice().isEmpty());

        // Ask price = 45002 and order price = 45005
        prepareNewOrderResponse("2", OrderStatus.FILLED, prepareTrade(List.of("0.2", "0.1", "0.1"), List.of("45002", "45004", "45005")));
        this.strategy.onTimerEvent(this.quoteTimer);
        this.apiHandler.flushNewOrderResponse(1);
        validateNextOrder(1, "0.4", 45005, OrderSide.BUY);

        // DB insertions
        Order order = validateNextDbOrder(null, OrderAction.SUBMIT, OrderState.SUBMIT, OrderSide.BUY, 45005, 0.4, 0.4, 0, 1);
        validateNextDbOrder("2", OrderAction.SUBMITTED, OrderState.SUBMITTED, OrderSide.BUY, 45005, 0.4, 0.4, 0, 2);
        validateNextDbOrder("2", OrderAction.SUBMITTED, OrderState.PARTIAL_EXEC, OrderSide.BUY, 45005, 0.4, 0.2, 0.2, 3);
        validateNextDbTrade(order.getId(), OrderSide.BUY, 45002, 0.2);
        validateNextDbOrder("2", OrderAction.SUBMITTED, OrderState.PARTIAL_EXEC, OrderSide.BUY, 45005, 0.4, 0.1, 0.3, 4);
        validateNextDbTrade(order.getId(), OrderSide.BUY, 45004, 0.1);
        validateNextDbOrder("2", OrderAction.EXECUTED, OrderState.EXECUTED, OrderSide.BUY, 45005, 0.4, 0, 0.4, 5);
        validateNextDbTrade(order.getId(), OrderSide.BUY, 45005, 0.1);

        // Assert data structures
        Assertions.assertTrue(this.strategy.getBuyOrders().get(45005d).isEmpty());
        Assertions.assertTrue(this.strategy.getSellOrders().get(45015d).contains(1));
        Assertions.assertEquals("0.4", this.strategy.getBidSizes().get(1));
        Assertions.assertEquals(0.4, this.strategy.getAskSizes().get(45015d).get(1));
        Assertions.assertTrue(this.strategy.getBoughtPrices().get(1).contains(45005d));
        Assertions.assertEquals(45005d, this.strategy.getAskToBidPrice().get(1).get(45015d));

        // No order should go out next
        this.strategy.onTimerEvent(this.quoteTimer);
        this.apiHandler.flushNewOrderResponse(1);
        this.apiHandler.hasNoOrder();
        this.dbAdapter.hasNoNextDbEntity();
    }

    @Test
    void test_BuyAndSell_CanceledResponse_Sell() throws Exception {
        // Assert data structures
        Assertions.assertTrue(this.strategy.getBuyOrders().get(45005d).contains(1));
        Assertions.assertTrue(this.strategy.getSellOrders().isEmpty());
        Assertions.assertEquals("0.4", this.strategy.getBidSizes().get(1));
        Assertions.assertTrue(this.strategy.getAskSizes().isEmpty());
        Assertions.assertTrue(this.strategy.getBoughtPrices().get(1).isEmpty());
        Assertions.assertTrue(this.strategy.getAskToBidPrice().isEmpty());

        // Ask price = 45002 and order price = 45005
        prepareNewOrderResponse("1", OrderStatus.FILLED, prepareTrade(List.of("0.2", "0.1", "0.1"), List.of("45002", "45004", "45005")));
        this.strategy.onTimerEvent(this.quoteTimer);
        this.apiHandler.flushNewOrderResponse(1);
        validateNextOrder(1, "0.4", 45005, OrderSide.BUY);

        // DB insertions
        Order order = validateNextDbOrder(null, OrderAction.SUBMIT, OrderState.SUBMIT, OrderSide.BUY, 45005, 0.4, 0.4, 0, 1);
        validateNextDbOrder("1", OrderAction.SUBMITTED, OrderState.SUBMITTED, OrderSide.BUY, 45005, 0.4, 0.4, 0, 2);
        validateNextDbOrder("1", OrderAction.SUBMITTED, OrderState.PARTIAL_EXEC, OrderSide.BUY, 45005, 0.4, 0.2, 0.2, 3);
        validateNextDbTrade(order.getId(), OrderSide.BUY, 45002, 0.2);
        validateNextDbOrder("1", OrderAction.SUBMITTED, OrderState.PARTIAL_EXEC, OrderSide.BUY, 45005, 0.4, 0.1, 0.3, 4);
        validateNextDbTrade(order.getId(), OrderSide.BUY, 45004, 0.1);
        validateNextDbOrder("1", OrderAction.EXECUTED, OrderState.EXECUTED, OrderSide.BUY, 45005, 0.4, 0, 0.4, 5);
        validateNextDbTrade(order.getId(), OrderSide.BUY, 45005, 0.1);

        // Assert data structures
        Assertions.assertTrue(this.strategy.getBuyOrders().get(45005d).isEmpty());
        Assertions.assertTrue(this.strategy.getSellOrders().get(45015d).contains(1));
        Assertions.assertEquals("0.4", this.strategy.getBidSizes().get(1));
        Assertions.assertEquals(0.4, this.strategy.getAskSizes().get(45015d).get(1));
        Assertions.assertTrue(this.strategy.getBoughtPrices().get(1).contains(45005d));
        Assertions.assertEquals(45005d, this.strategy.getAskToBidPrice().get(1).get(45015d));

        // No order should go out next
        this.strategy.onTimerEvent(this.quoteTimer);
        this.apiHandler.flushNewOrderResponse(1);
        this.apiHandler.hasNoOrder();
        this.dbAdapter.hasNoNextDbEntity();

        // Assert data structures
        Assertions.assertTrue(this.strategy.getBuyOrders().get(45005d).isEmpty());
        Assertions.assertTrue(this.strategy.getBuyOrders().get(44990d).contains(1));
        Assertions.assertTrue(this.strategy.getSellOrders().get(45015d).contains(1));
        Assertions.assertEquals("0.4", this.strategy.getBidSizes().get(1));
        Assertions.assertEquals(0.4, this.strategy.getAskSizes().get(45015d).get(1));
        Assertions.assertTrue(this.strategy.getBoughtPrices().get(1).contains(45005d));
        Assertions.assertEquals(45005d, this.strategy.getAskToBidPrice().get(1).get(45015d));

        // Quote updated to issue sell order
        Mockito.when(this.quote.getBidPrice()).thenReturn(45016d);
        Mockito.when(this.quote.getAskPrice()).thenReturn(45017d);

        // Bid price = 45016 and order price = 45015
        prepareNewOrderResponse("2", OrderStatus.CANCELED, null);
        this.strategy.onTimerEvent(this.quoteTimer);
        this.apiHandler.flushNewOrderResponse(1);
        validateNextOrder(1, "0.4", 45015, OrderSide.SELL);

        // DB insertions
        order = validateNextDbOrder(null, OrderAction.SUBMIT, OrderState.SUBMIT, OrderSide.SELL, 45015, 0.4, 0.4, 0, 1);
        validateNextDbOrder("2", OrderAction.SUBMITTED, OrderState.SUBMITTED, OrderSide.SELL, 45015, 0.4, 0.4, 0, 2);
        validateNextDbOrder("2", OrderAction.CANCELED, OrderState.CANCELED, OrderSide.SELL, 45015, 0.4, 0, 0, 3);

        // Assert data structures
        Assertions.assertTrue(this.strategy.getBuyOrders().get(45005d).isEmpty());
        Assertions.assertTrue(this.strategy.getBuyOrders().get(44990d).contains(1));
        Assertions.assertTrue(this.strategy.getSellOrders().get(45015d).contains(1));
        Assertions.assertEquals("0.4", this.strategy.getBidSizes().get(1));
        Assertions.assertTrue(this.strategy.getAskSizes().get(45015d).containsKey(1));
        Assertions.assertTrue(this.strategy.getBoughtPrices().get(1).contains(45005d));
        Assertions.assertEquals(45005d, this.strategy.getAskToBidPrice().get(1).get(45015d));

        // Bid price = 45016 and order price = 45015
        prepareNewOrderResponse("3", OrderStatus.CANCELED, null);
        this.strategy.onTimerEvent(this.quoteTimer);
        this.apiHandler.flushNewOrderResponse(1);
        validateNextOrder(1, "0.4", 45015, OrderSide.SELL);

        // DB insertions
        order = validateNextDbOrder(null, OrderAction.SUBMIT, OrderState.SUBMIT, OrderSide.SELL, 45015, 0.4, 0.4, 0, 1);
        validateNextDbOrder("3", OrderAction.SUBMITTED, OrderState.SUBMITTED, OrderSide.SELL, 45015, 0.4, 0.4, 0, 2);
        validateNextDbOrder("3", OrderAction.CANCELED, OrderState.CANCELED, OrderSide.SELL, 45015, 0.4, 0, 0, 3);
    }

    @Test
    void test_BuyAndSell_SubmitReject_Buy() throws Exception {
        // Assert data structures
        Assertions.assertTrue(this.strategy.getBuyOrders().get(45005d).contains(1));
        Assertions.assertTrue(this.strategy.getSellOrders().isEmpty());
        Assertions.assertEquals("0.4", this.strategy.getBidSizes().get(1));
        Assertions.assertTrue(this.strategy.getAskSizes().isEmpty());
        Assertions.assertTrue(this.strategy.getBoughtPrices().get(1).isEmpty());
        Assertions.assertTrue(this.strategy.getAskToBidPrice().isEmpty());

        // Ask price = 45002 and order price = 45005
        prepareNewOrderResponse("1", OrderStatus.REJECTED, null);
        this.strategy.onTimerEvent(this.quoteTimer);
        this.apiHandler.flushNewOrderResponse(1);
        validateNextOrder(1, "0.4", 45005, OrderSide.BUY);

        // DB insertions
        validateNextDbOrder(null, OrderAction.SUBMIT, OrderState.SUBMIT, OrderSide.BUY, 45005, 0.4, 0.4, 0, 1);
        validateNextDbOrder("1", OrderAction.SUBMIT_REJECTED, OrderState.SUBMIT_REJECTED, OrderSide.BUY, 45005, 0.4, 0.4, 0, 2);

        // Assert data structures
        Assertions.assertTrue(this.strategy.getBuyOrders().get(45005d).contains(1));
        Assertions.assertTrue(this.strategy.getSellOrders().isEmpty());
        Assertions.assertEquals("0.4", this.strategy.getBidSizes().get(1));
        Assertions.assertTrue(this.strategy.getAskSizes().isEmpty());
        Assertions.assertTrue(this.strategy.getBoughtPrices().get(1).isEmpty());
        Assertions.assertTrue(this.strategy.getAskToBidPrice().isEmpty());

        // Ask price = 45002 and order price = 45005
        prepareNewOrderResponse("1", OrderStatus.REJECTED, null);
        this.strategy.onTimerEvent(this.quoteTimer);
        this.apiHandler.flushNewOrderResponse(1);
        validateNextOrder(1, "0.4", 45005, OrderSide.BUY);

        // DB insertions
        validateNextDbOrder(null, OrderAction.SUBMIT, OrderState.SUBMIT, OrderSide.BUY, 45005, 0.4, 0.4, 0, 1);
        validateNextDbOrder("1", OrderAction.SUBMIT_REJECTED, OrderState.SUBMIT_REJECTED, OrderSide.BUY, 45005, 0.4, 0.4, 0, 2);

        // Assert data structures
        Assertions.assertTrue(this.strategy.getBuyOrders().get(45005d).contains(1));
        Assertions.assertTrue(this.strategy.getSellOrders().isEmpty());
        Assertions.assertEquals("0.4", this.strategy.getBidSizes().get(1));
        Assertions.assertTrue(this.strategy.getAskSizes().isEmpty());
        Assertions.assertTrue(this.strategy.getBoughtPrices().get(1).isEmpty());
        Assertions.assertTrue(this.strategy.getAskToBidPrice().isEmpty());
    }

    @Test
    void test_BuyAndSell_SubmitReject_Sell() throws Exception {
        // Assert data structures
        Assertions.assertTrue(this.strategy.getBuyOrders().get(45005d).contains(1));
        Assertions.assertTrue(this.strategy.getSellOrders().isEmpty());
        Assertions.assertEquals("0.4", this.strategy.getBidSizes().get(1));
        Assertions.assertTrue(this.strategy.getAskSizes().isEmpty());
        Assertions.assertTrue(this.strategy.getBoughtPrices().get(1).isEmpty());
        Assertions.assertTrue(this.strategy.getAskToBidPrice().isEmpty());

        // Ask price = 45002 and order price = 45005
        prepareNewOrderResponse("1", OrderStatus.FILLED, prepareTrade(List.of("0.2", "0.1", "0.1"), List.of("45002", "45004", "45005")));
        this.strategy.onTimerEvent(this.quoteTimer);
        this.apiHandler.flushNewOrderResponse(1);
        validateNextOrder(1, "0.4", 45005, OrderSide.BUY);

        // DB insertions
        Order order = validateNextDbOrder(null, OrderAction.SUBMIT, OrderState.SUBMIT, OrderSide.BUY, 45005, 0.4, 0.4, 0, 1);
        validateNextDbOrder("1", OrderAction.SUBMITTED, OrderState.SUBMITTED, OrderSide.BUY, 45005, 0.4, 0.4, 0, 2);
        validateNextDbOrder("1", OrderAction.SUBMITTED, OrderState.PARTIAL_EXEC, OrderSide.BUY, 45005, 0.4, 0.2, 0.2, 3);
        validateNextDbTrade(order.getId(), OrderSide.BUY, 45002, 0.2);
        validateNextDbOrder("1", OrderAction.SUBMITTED, OrderState.PARTIAL_EXEC, OrderSide.BUY, 45005, 0.4, 0.1, 0.3, 4);
        validateNextDbTrade(order.getId(), OrderSide.BUY, 45004, 0.1);
        validateNextDbOrder("1", OrderAction.EXECUTED, OrderState.EXECUTED, OrderSide.BUY, 45005, 0.4, 0, 0.4, 5);
        validateNextDbTrade(order.getId(), OrderSide.BUY, 45005, 0.1);

        // Assert data structures
        Assertions.assertTrue(this.strategy.getBuyOrders().get(45005d).isEmpty());
        Assertions.assertTrue(this.strategy.getSellOrders().get(45015d).contains(1));
        Assertions.assertEquals("0.4", this.strategy.getBidSizes().get(1));
        Assertions.assertEquals(0.4, this.strategy.getAskSizes().get(45015d).get(1));
        Assertions.assertTrue(this.strategy.getBoughtPrices().get(1).contains(45005d));
        Assertions.assertEquals(45005d, this.strategy.getAskToBidPrice().get(1).get(45015d));

        // No order should go out next
        this.strategy.onTimerEvent(this.quoteTimer);
        this.apiHandler.flushNewOrderResponse(1);
        this.apiHandler.hasNoOrder();
        this.dbAdapter.hasNoNextDbEntity();

        // Assert data structures
        Assertions.assertTrue(this.strategy.getBuyOrders().get(45005d).isEmpty());
        Assertions.assertTrue(this.strategy.getBuyOrders().get(44990d).contains(1));
        Assertions.assertTrue(this.strategy.getSellOrders().get(45015d).contains(1));
        Assertions.assertEquals("0.4", this.strategy.getBidSizes().get(1));
        Assertions.assertEquals(0.4, this.strategy.getAskSizes().get(45015d).get(1));
        Assertions.assertTrue(this.strategy.getBoughtPrices().get(1).contains(45005d));
        Assertions.assertEquals(45005d, this.strategy.getAskToBidPrice().get(1).get(45015d));

        // Quote updated to issue sell order
        Mockito.when(this.quote.getBidPrice()).thenReturn(45016d);
        Mockito.when(this.quote.getAskPrice()).thenReturn(45017d);

        // Bid price = 45016 and order price = 45015
        prepareNewOrderResponse("2", OrderStatus.REJECTED, null);
        this.strategy.onTimerEvent(this.quoteTimer);
        this.apiHandler.flushNewOrderResponse(1);
        validateNextOrder(1, "0.4", 45015, OrderSide.SELL);

        // DB insertions
        validateNextDbOrder(null, OrderAction.SUBMIT, OrderState.SUBMIT, OrderSide.SELL, 45015, 0.4, 0.4, 0, 1);
        validateNextDbOrder("2", OrderAction.SUBMIT_REJECTED, OrderState.SUBMIT_REJECTED, OrderSide.SELL, 45015, 0.4, 0.4, 0, 2);

        // Assert data structures
        Assertions.assertTrue(this.strategy.getBuyOrders().get(45005d).isEmpty());
        Assertions.assertTrue(this.strategy.getBuyOrders().get(44990d).contains(1));
        Assertions.assertTrue(this.strategy.getSellOrders().get(45015d).contains(1));
        Assertions.assertEquals("0.4", this.strategy.getBidSizes().get(1));
        Assertions.assertEquals(0.4, this.strategy.getAskSizes().get(45015d).get(1));
        Assertions.assertTrue(this.strategy.getBoughtPrices().get(1).contains(45005d));
        Assertions.assertEquals(45005d, this.strategy.getAskToBidPrice().get(1).get(45015d));
    }

    @Test
    void test_QuickDip() throws Exception {
        // Assert data structures
        Assertions.assertTrue(this.strategy.getBuyOrders().get(45005d).contains(1));
        Assertions.assertTrue(this.strategy.getSellOrders().isEmpty());
        Assertions.assertEquals("0.4", this.strategy.getBidSizes().get(1));
        Assertions.assertTrue(this.strategy.getAskSizes().isEmpty());
        Assertions.assertTrue(this.strategy.getBoughtPrices().get(1).isEmpty());
        Assertions.assertTrue(this.strategy.getAskToBidPrice().isEmpty());

        // Ask price = 45002 and order price = 45005
        prepareNewOrderResponse("1", OrderStatus.FILLED, prepareTrade(List.of("0.2", "0.1", "0.1"), List.of("45002", "45004", "45005")));
        this.strategy.onTimerEvent(this.quoteTimer);
        this.apiHandler.flushNewOrderResponse(1);
        validateNextOrder(1, "0.4", 45005, OrderSide.BUY);

        // DB insertions
        Order order = validateNextDbOrder(null, OrderAction.SUBMIT, OrderState.SUBMIT, OrderSide.BUY, 45005, 0.4, 0.4, 0, 1);
        validateNextDbOrder("1", OrderAction.SUBMITTED, OrderState.SUBMITTED, OrderSide.BUY, 45005, 0.4, 0.4, 0, 2);
        validateNextDbOrder("1", OrderAction.SUBMITTED, OrderState.PARTIAL_EXEC, OrderSide.BUY, 45005, 0.4, 0.2, 0.2, 3);
        validateNextDbTrade(order.getId(), OrderSide.BUY, 45002, 0.2);
        validateNextDbOrder("1", OrderAction.SUBMITTED, OrderState.PARTIAL_EXEC, OrderSide.BUY, 45005, 0.4, 0.1, 0.3, 4);
        validateNextDbTrade(order.getId(), OrderSide.BUY, 45004, 0.1);
        validateNextDbOrder("1", OrderAction.EXECUTED, OrderState.EXECUTED, OrderSide.BUY, 45005, 0.4, 0, 0.4, 5);
        validateNextDbTrade(order.getId(), OrderSide.BUY, 45005, 0.1);

        // Assert data structures
        Assertions.assertTrue(this.strategy.getBuyOrders().get(45005d).isEmpty());
        Assertions.assertTrue(this.strategy.getSellOrders().get(45015d).contains(1));
        Assertions.assertEquals("0.4", this.strategy.getBidSizes().get(1));
        Assertions.assertEquals(0.4, this.strategy.getAskSizes().get(45015d).get(1));
        Assertions.assertTrue(this.strategy.getBoughtPrices().get(1).contains(45005d));
        Assertions.assertEquals(45005d, this.strategy.getAskToBidPrice().get(1).get(45015d));

        // Quote updated to issue more buy orders
        Mockito.when(this.quote.getBidPrice()).thenReturn(44955d);
        Mockito.when(this.quote.getAskPrice()).thenReturn(44957d);

        // Ask price = 44957d and order price = 44990
        prepareNewOrderResponse("2", OrderStatus.FILLED, prepareTrade(List.of("0.15", "0.25"), List.of("44957", "44958")));
        this.strategy.onTimerEvent(this.quoteTimer);
        this.apiHandler.flushNewOrderResponse(1);
        validateNextOrder(1, "0.4", 44990, OrderSide.BUY);

        // DB insertions
        order = validateNextDbOrder(null, OrderAction.SUBMIT, OrderState.SUBMIT, OrderSide.BUY, 44990, 0.4, 0.4, 0, 1);
        validateNextDbOrder("2", OrderAction.SUBMITTED, OrderState.SUBMITTED, OrderSide.BUY, 44990, 0.4, 0.4, 0, 2);
        validateNextDbOrder("2", OrderAction.SUBMITTED, OrderState.PARTIAL_EXEC, OrderSide.BUY, 44990, 0.4, 0.25, 0.15, 3);
        validateNextDbTrade(order.getId(), OrderSide.BUY, 44957, 0.15);
        validateNextDbOrder("2", OrderAction.EXECUTED, OrderState.EXECUTED, OrderSide.BUY, 44990, 0.4, 0, 0.4, 4);
        validateNextDbTrade(order.getId(), OrderSide.BUY, 44958, 0.25);

        // Assert data structures
        Assertions.assertTrue(this.strategy.getBuyOrders().get(45005d).isEmpty());
        Assertions.assertTrue(this.strategy.getBuyOrders().get(44990d).isEmpty());
        Assertions.assertTrue(this.strategy.getBuyOrders().get(44960d).contains(1));
        Assertions.assertTrue(this.strategy.getSellOrders().get(45015d).contains(1));
        Assertions.assertTrue(this.strategy.getSellOrders().get(45000d).contains(1));
        Assertions.assertEquals("0.4", this.strategy.getBidSizes().get(1));
        Assertions.assertEquals(0.4, this.strategy.getAskSizes().get(45015d).get(1));
        Assertions.assertEquals(0.4, this.strategy.getAskSizes().get(45000d).get(1));
        Assertions.assertTrue(this.strategy.getBoughtPrices().get(1).contains(45005d));
        Assertions.assertTrue(this.strategy.getBoughtPrices().get(1).contains(44990d));
        Assertions.assertEquals(45005d, this.strategy.getAskToBidPrice().get(1).get(45015d));
        Assertions.assertEquals(44990d, this.strategy.getAskToBidPrice().get(1).get(45000d));

        // Ask price = 45002 and order price = 44975
        prepareNewOrderResponse("3", OrderStatus.FILLED, prepareTrade(List.of("0.15", "0.25"), List.of("44959", "44960")));
        this.strategy.onTimerEvent(this.quoteTimer);
        this.apiHandler.flushNewOrderResponse(1);
        validateNextOrder(1, "0.4", 44960, OrderSide.BUY);

        // DB insertions
        order = validateNextDbOrder(null, OrderAction.SUBMIT, OrderState.SUBMIT, OrderSide.BUY, 44960, 0.4, 0.4, 0, 1);
        validateNextDbOrder("3", OrderAction.SUBMITTED, OrderState.SUBMITTED, OrderSide.BUY, 44960, 0.4, 0.4, 0, 2);
        validateNextDbOrder("3", OrderAction.SUBMITTED, OrderState.PARTIAL_EXEC, OrderSide.BUY, 44960, 0.4, 0.25, 0.15, 3);
        validateNextDbTrade(order.getId(), OrderSide.BUY, 44959, 0.15);
        validateNextDbOrder("3", OrderAction.EXECUTED, OrderState.EXECUTED, OrderSide.BUY, 44960, 0.4, 0, 0.4, 4);
        validateNextDbTrade(order.getId(), OrderSide.BUY, 44960, 0.25);

        // Assert data structures
        Assertions.assertTrue(this.strategy.getBuyOrders().get(45005d).isEmpty());
        Assertions.assertTrue(this.strategy.getBuyOrders().get(44990d).isEmpty());
        Assertions.assertTrue(this.strategy.getBuyOrders().get(44960d).isEmpty());
        Assertions.assertTrue(this.strategy.getBuyOrders().get(44945d).contains(1));
        Assertions.assertTrue(this.strategy.getSellOrders().get(45015d).contains(1));
        Assertions.assertTrue(this.strategy.getSellOrders().get(45000d).contains(1));
        Assertions.assertTrue(this.strategy.getSellOrders().get(44970d).contains(1));
        Assertions.assertEquals("0.4", this.strategy.getBidSizes().get(1));
        Assertions.assertEquals(0.4, this.strategy.getAskSizes().get(45015d).get(1));
        Assertions.assertEquals(0.4, this.strategy.getAskSizes().get(45000d).get(1));
        Assertions.assertEquals(0.4, this.strategy.getAskSizes().get(44970d).get(1));
        Assertions.assertTrue(this.strategy.getBoughtPrices().get(1).contains(45005d));
        Assertions.assertTrue(this.strategy.getBoughtPrices().get(1).contains(44990d));
        Assertions.assertTrue(this.strategy.getBoughtPrices().get(1).contains(44960d));
        Assertions.assertEquals(45005d, this.strategy.getAskToBidPrice().get(1).get(45015d));
        Assertions.assertEquals(44990d, this.strategy.getAskToBidPrice().get(1).get(45000d));
        Assertions.assertEquals(44960d, this.strategy.getAskToBidPrice().get(1).get(44970d));

        // Issues no further buy or sell orders
        this.strategy.onTimerEvent(this.quoteTimer);
        this.apiHandler.flushNewOrderResponse(1);
        this.apiHandler.hasNoOrder();
        this.dbAdapter.hasNoNextDbEntity();
    }

    @Test
    void test_QuickRise() throws Exception {
        // Assert data structures
        Assertions.assertTrue(this.strategy.getBuyOrders().get(45005d).contains(1));
        Assertions.assertTrue(this.strategy.getSellOrders().isEmpty());
        Assertions.assertEquals("0.4", this.strategy.getBidSizes().get(1));
        Assertions.assertTrue(this.strategy.getAskSizes().isEmpty());
        Assertions.assertTrue(this.strategy.getBoughtPrices().get(1).isEmpty());
        Assertions.assertTrue(this.strategy.getAskToBidPrice().isEmpty());

        // Ask price = 45002 and order price = 45005
        prepareNewOrderResponse("1", OrderStatus.FILLED, prepareTrade(List.of("0.2", "0.1", "0.1"), List.of("45002", "45004", "45005")));
        this.strategy.onTimerEvent(this.quoteTimer);
        this.apiHandler.flushNewOrderResponse(1);
        validateNextOrder(1, "0.4", 45005, OrderSide.BUY);

        // DB insertions
        Order order = validateNextDbOrder(null, OrderAction.SUBMIT, OrderState.SUBMIT, OrderSide.BUY, 45005, 0.4, 0.4, 0, 1);
        validateNextDbOrder("1", OrderAction.SUBMITTED, OrderState.SUBMITTED, OrderSide.BUY, 45005, 0.4, 0.4, 0, 2);
        validateNextDbOrder("1", OrderAction.SUBMITTED, OrderState.PARTIAL_EXEC, OrderSide.BUY, 45005, 0.4, 0.2, 0.2, 3);
        validateNextDbTrade(order.getId(), OrderSide.BUY, 45002, 0.2);
        validateNextDbOrder("1", OrderAction.SUBMITTED, OrderState.PARTIAL_EXEC, OrderSide.BUY, 45005, 0.4, 0.1, 0.3, 4);
        validateNextDbTrade(order.getId(), OrderSide.BUY, 45004, 0.1);
        validateNextDbOrder("1", OrderAction.EXECUTED, OrderState.EXECUTED, OrderSide.BUY, 45005, 0.4, 0, 0.4, 5);
        validateNextDbTrade(order.getId(), OrderSide.BUY, 45005, 0.1);

        // Assert data structures
        Assertions.assertTrue(this.strategy.getBuyOrders().get(45005d).isEmpty());
        Assertions.assertTrue(this.strategy.getSellOrders().get(45015d).contains(1));
        Assertions.assertEquals("0.4", this.strategy.getBidSizes().get(1));
        Assertions.assertEquals(0.4, this.strategy.getAskSizes().get(45015d).get(1));
        Assertions.assertTrue(this.strategy.getBoughtPrices().get(1).contains(45005d));
        Assertions.assertEquals(45005d, this.strategy.getAskToBidPrice().get(1).get(45015d));

        // Quote updated to issue more buy orders
        Mockito.when(this.quote.getBidPrice()).thenReturn(45030d);
        Mockito.when(this.quote.getAskPrice()).thenReturn(45032d);

        // Bid price = 45032 and order price = 45015
        prepareNewOrderResponse("2", OrderStatus.FILLED, prepareTrade(List.of("0.15", "0.25"), List.of("45014", "45013")));
        this.strategy.onTimerEvent(this.quoteTimer);
        this.apiHandler.flushNewOrderResponse(1);
        validateNextOrder(1, "0.4", 45015, OrderSide.SELL);

        // DB insertions
        order = validateNextDbOrder(null, OrderAction.SUBMIT, OrderState.SUBMIT, OrderSide.SELL, 45015, 0.4, 0.4, 0, 1);
        validateNextDbOrder("2", OrderAction.SUBMITTED, OrderState.SUBMITTED, OrderSide.SELL, 45015, 0.4, 0.4, 0, 2);
        validateNextDbOrder("2", OrderAction.SUBMITTED, OrderState.PARTIAL_EXEC, OrderSide.SELL, 45015, 0.4, 0.25, 0.15, 3);
        validateNextDbTrade(order.getId(), OrderSide.SELL, 45014, 0.15);
        validateNextDbOrder("2", OrderAction.EXECUTED, OrderState.EXECUTED, OrderSide.SELL, 45015, 0.4, 0, 0.4, 4);
        validateNextDbTrade(order.getId(), OrderSide.SELL, 45013, 0.25);

        // Assert data structures
        Assertions.assertTrue(this.strategy.getBuyOrders().get(45005d).contains(1));
        Assertions.assertTrue(this.strategy.getBuyOrders().get(44990d).contains(1));
        Assertions.assertTrue(this.strategy.getSellOrders().get(45015d).isEmpty());
        Assertions.assertEquals("0.4", this.strategy.getBidSizes().get(1));
        Assertions.assertTrue(this.strategy.getAskSizes().get(45015d).isEmpty());
        Assertions.assertTrue(this.strategy.getBoughtPrices().get(1).isEmpty());
        Assertions.assertTrue(this.strategy.getAskToBidPrice().get(1).isEmpty());

        // No order should go out next
        this.strategy.onTimerEvent(this.quoteTimer);
        this.apiHandler.flushNewOrderResponse(1);
        this.apiHandler.hasNoOrder();
        this.dbAdapter.hasNoNextDbEntity();

        // Assert data structures
        Assertions.assertTrue(this.strategy.getBuyOrders().get(45005d).contains(1));
        Assertions.assertTrue(this.strategy.getBuyOrders().get(44990d).contains(1));
        Assertions.assertTrue(this.strategy.getSellOrders().get(45015d).isEmpty());
        Assertions.assertEquals("0.4", this.strategy.getBidSizes().get(1));
        Assertions.assertTrue(this.strategy.getAskSizes().get(45015d).isEmpty());
        Assertions.assertTrue(this.strategy.getBoughtPrices().get(1).isEmpty());
        Assertions.assertTrue(this.strategy.getAskToBidPrice().get(1).isEmpty());

        // Health check - place a buy order at 45035
        this.strategy.onTimerEvent(this.healthCheckTimer);
        this.apiHandler.flushNewOrderResponse(1);
        this.apiHandler.hasNoOrder();
        this.dbAdapter.hasNoNextDbEntity();

        // Assert data structures
        Assertions.assertTrue(this.strategy.getBuyOrders().get(45035d).contains(1));
        Assertions.assertTrue(this.strategy.getBuyOrders().get(45005d).contains(1));
        Assertions.assertTrue(this.strategy.getBuyOrders().get(44990d).contains(1));
        Assertions.assertTrue(this.strategy.getSellOrders().get(45015d).isEmpty());
        Assertions.assertEquals("0.4", this.strategy.getBidSizes().get(1));
        Assertions.assertTrue(this.strategy.getAskSizes().get(45015d).isEmpty());
        Assertions.assertTrue(this.strategy.getBoughtPrices().get(1).isEmpty());
        Assertions.assertTrue(this.strategy.getAskToBidPrice().get(1).isEmpty());

        // Bid price = 45032 and order price = 45035
        prepareNewOrderResponse("3", OrderStatus.FILLED, prepareTrade(List.of("0.15", "0.2", "0.05"), List.of("45031", "45032", "45033")));
        this.strategy.onTimerEvent(this.quoteTimer);
        this.apiHandler.flushNewOrderResponse(1);
        validateNextOrder(1, "0.4", 45035, OrderSide.BUY);

        // DB insertions
        order = validateNextDbOrder(null, OrderAction.SUBMIT, OrderState.SUBMIT, OrderSide.BUY, 45035, 0.4, 0.4, 0, 1);
        validateNextDbOrder("3", OrderAction.SUBMITTED, OrderState.SUBMITTED, OrderSide.BUY, 45035, 0.4, 0.4, 0, 2);
        validateNextDbOrder("3", OrderAction.SUBMITTED, OrderState.PARTIAL_EXEC, OrderSide.BUY, 45035, 0.4, 0.25, 0.15, 3);
        validateNextDbTrade(order.getId(), OrderSide.BUY, 45031, 0.15);
        validateNextDbOrder("3", OrderAction.SUBMITTED, OrderState.PARTIAL_EXEC, OrderSide.BUY, 45035, 0.4, 0.05, 0.35, 4);
        validateNextDbTrade(order.getId(), OrderSide.BUY, 45032, 0.2);
        validateNextDbOrder("3", OrderAction.EXECUTED, OrderState.EXECUTED, OrderSide.BUY, 45035, 0.4, 0, 0.4, 5);
        validateNextDbTrade(order.getId(), OrderSide.BUY, 45033, 0.05);

        // Assert data structures
        Assertions.assertTrue(this.strategy.getBuyOrders().get(45035d).isEmpty());
        Assertions.assertTrue(this.strategy.getBuyOrders().get(45005d).contains(1));
        Assertions.assertTrue(this.strategy.getBuyOrders().get(44990d).contains(1));
        Assertions.assertTrue(this.strategy.getSellOrders().get(45045d).contains(1));
        Assertions.assertTrue(this.strategy.getSellOrders().get(45015d).isEmpty());
        Assertions.assertEquals("0.4", this.strategy.getBidSizes().get(1));
        Assertions.assertEquals(0.4, this.strategy.getAskSizes().get(45045d).get(1));
        Assertions.assertTrue(this.strategy.getBoughtPrices().get(1).contains(45035d));
        Assertions.assertEquals(45035d, this.strategy.getAskToBidPrice().get(1).get(45045d));

        // Issues no further buy or sell orders
        this.strategy.onTimerEvent(this.quoteTimer);
        this.apiHandler.flushNewOrderResponse(1);
        this.apiHandler.hasNoOrder();
        this.dbAdapter.hasNoNextDbEntity();
    }
}
