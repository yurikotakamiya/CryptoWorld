package cw.trader.handler.binance;

import com.binance.api.client.BinanceApiAsyncRestClient;
import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.domain.OrderStatus;
import com.binance.api.client.domain.TimeInForce;
import com.binance.api.client.domain.account.NewOrder;
import com.binance.api.client.domain.account.NewOrderResponse;
import com.binance.api.client.domain.account.Trade;
import cw.common.db.mysql.ApiKey;
import cw.common.md.Exchange;
import cw.common.order.OrderAction;
import cw.common.order.OrderSide;
import cw.common.order.OrderState;
import cw.trader.ExchangeApiHandler;
import cw.trader.OrderResponse;
import cw.trader.strategy.AbstractTraderStrategy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class BinanceApiHandler extends ExchangeApiHandler {
    private static final Logger LOGGER = LogManager.getLogger(BinanceApiHandler.class.getSimpleName());

    protected final Map<Integer, BinanceApiAsyncRestClient> asyncRestClients;

    public BinanceApiHandler() {
        this.asyncRestClients = new HashMap<>();
    }

    public void handleNewOrderResponse(NewOrderResponse newOrderResponse, AbstractTraderStrategy strategy, int userId, long orderId, double orderPrice, OrderSide orderSide) {
        strategy.updateOrderClientOrderId(orderId, newOrderResponse.getClientOrderId());

        OrderResponse orderResponse = new OrderResponse();
        orderResponse.strategyId = strategy.getId();
        orderResponse.orderId = orderId;
        orderResponse.userId = userId;
        orderResponse.clientOrderId = newOrderResponse.getClientOrderId();

        OrderStatus orderStatus = newOrderResponse.getStatus();

        if (orderStatus == OrderStatus.NEW) {
            orderResponse.orderAction = OrderAction.SUBMITTED;
            orderResponse.orderState = OrderState.SUBMITTED;
        } else if (orderStatus == OrderStatus.PARTIALLY_FILLED) {
            orderResponse.orderAction = OrderAction.SUBMITTED;
            orderResponse.orderState = OrderState.PARTIAL_EXEC;
        } else if (orderStatus == OrderStatus.FILLED) {
            orderResponse.orderAction = OrderAction.EXECUTED;
            orderResponse.orderState = OrderState.EXECUTED;
        } else if (orderStatus == OrderStatus.CANCELED) {
            orderResponse.orderAction = OrderAction.CANCELED;
            orderResponse.orderState = OrderState.CANCELED;
        } else if (orderStatus == OrderStatus.REJECTED) {
            orderResponse.orderAction = OrderAction.SUBMIT_REJECTED;
            orderResponse.orderState = OrderState.SUBMIT_REJECTED;
        }

        if (orderSide == OrderSide.BUY) {
            orderResponse.bidPrice = orderPrice;
        } else if (orderSide == OrderSide.SELL) {
            orderResponse.askPrice = orderPrice;
        }

        orderResponse.transactionTime = newOrderResponse.getTransactTime();

        if (newOrderResponse.getFills() != null) {
            for (Trade trade : newOrderResponse.getFills()) {
                orderResponse.executedQuantities.add(Double.parseDouble(trade.getQty()));
                orderResponse.executionPrice.add(Double.parseDouble(trade.getPrice()));
            }
        }

        this.enqueueCallback.accept(orderResponse);
    }

    @Override
    public Exchange getExchange() {
        return Exchange.BINANCE;
    }

    @Override
    public void add(ApiKey apiKey) {
        BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance(apiKey.getApiKey(), apiKey.getSecretKey());
        BinanceApiAsyncRestClient asyncRestClient = factory.newAsyncRestClient();
        this.asyncRestClients.put(apiKey.getUserId(), asyncRestClient);

        LOGGER.info("API key received for user {}.", apiKey.getUserId());
    }

    @Override
    public void submitLimitFok(AbstractTraderStrategy strategy, int userId, long orderId, String orderSize, String orderPrice, double orderPriceDouble, OrderSide orderSide) {
        BinanceApiAsyncRestClient asyncRestClient = this.asyncRestClients.get(userId);

        if (asyncRestClient == null) {
            LOGGER.error("Client not found for user {}.", userId);
        }

        NewOrder newOrder = NewOrder.limitBuy(strategy.getTradingPair().getExchangeToSymbolMap().get(strategy.getExchange()), TimeInForce.FOK, orderSize, orderPrice);
        asyncRestClient.newOrder(newOrder, r -> handleNewOrderResponse(r, strategy, userId, orderId, orderPriceDouble, orderSide));
    }
}
