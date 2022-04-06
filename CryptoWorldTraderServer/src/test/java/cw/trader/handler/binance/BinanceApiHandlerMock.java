package cw.trader.handler.binance;

import com.binance.api.client.BinanceApiAsyncRestClient;
import com.binance.api.client.domain.account.NewOrderResponse;
import cw.common.db.mysql.ApiKey;
import cw.common.db.mysql.OrderSide;
import cw.trader.OrderInfo;
import cw.trader.strategy.AbstractTraderStrategy;

import java.util.LinkedList;
import java.util.List;

public class BinanceApiHandlerMock extends BinanceApiHandler {
    private List<OrderInfo> orders;

    public BinanceApiHandlerMock() {
        this.orders = new LinkedList<>();
    }

    @Override
    public void add(ApiKey apiKey) {
        BinanceApiAsyncRestClientMock asyncRestClient = new BinanceApiAsyncRestClientMock();
        this.asyncRestClients.put(apiKey.getUserId(), asyncRestClient);
    }

    @Override
    public void submitLimitFok(AbstractTraderStrategy strategy, int userId, long orderId, String orderSize, String orderPrice, double orderPriceDouble, OrderSide orderSide) {
        super.submitLimitFok(strategy, userId, orderId, orderSize, orderPrice, orderPriceDouble, orderSide);

        OrderInfo orderInfo = new OrderInfo();
        orderInfo.userId = userId;
        orderInfo.orderSize = orderSize;
        orderInfo.orderPrice = orderPriceDouble;
        orderInfo.orderSide = orderSide;
        this.orders.add(orderInfo);
    }

    public OrderInfo nextOrder() {
        return this.orders.remove(0);
    }

    public boolean hasNoOrder() {
        return this.orders.isEmpty();
    }

    public void prepareNewOrderResponse(NewOrderResponse newOrderResponse) {
        for (BinanceApiAsyncRestClient asyncRestClient : this.asyncRestClients.values()) {
            BinanceApiAsyncRestClientMock mock = (BinanceApiAsyncRestClientMock) asyncRestClient;
            mock.newOrderResponses.add(newOrderResponse);
        }
    }

    public void flushNewOrderResponse(int userId) {
        BinanceApiAsyncRestClientMock asyncRestClientMock = (BinanceApiAsyncRestClientMock) this.asyncRestClients.get(userId);
        if (asyncRestClientMock == null) return;

        asyncRestClientMock.flushNewOrderResponses();
    }
}
