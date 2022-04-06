package cw.trader;

import cw.common.db.mysql.ApiKey;
import cw.common.db.mysql.StrategyConfig;
import cw.common.db.mysql.Exchange;
import cw.common.db.mysql.OrderSide;
import cw.trader.strategy.AbstractTraderStrategy;

import java.util.function.Consumer;

public abstract class ExchangeApiHandler {
    protected Consumer<OrderResponse> enqueueCallback;

    public boolean validate(StrategyConfig strategyConfig) {
        return (Exchange.values()[strategyConfig.getExchange()] == getExchange());
    }

    public void setEnqueueCallback(Consumer<OrderResponse> enqueueCallback) {
        this.enqueueCallback = enqueueCallback;
    }

    public abstract Exchange getExchange();

    public abstract void add(ApiKey apiKey);

    public abstract void submitLimitFok(AbstractTraderStrategy strategy, int userId, long orderId, String orderSize, String orderPrice, double orderPriceDouble, OrderSide orderSide);
}
