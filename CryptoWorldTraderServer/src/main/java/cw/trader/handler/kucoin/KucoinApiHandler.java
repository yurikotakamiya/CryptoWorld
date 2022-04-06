package cw.trader.handler.kucoin;

import cw.common.db.mysql.ApiKey;
import cw.common.db.mysql.Exchange;
import cw.common.db.mysql.OrderSide;
import cw.trader.ExchangeApiHandler;
import cw.trader.strategy.AbstractTraderStrategy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class KucoinApiHandler extends ExchangeApiHandler {
    private static final Logger LOGGER = LogManager.getLogger(KucoinApiHandler.class.getSimpleName());

    @Override
    public Exchange getExchange() {
        return Exchange.KUCOIN;
    }

    @Override
    public void add(ApiKey apiKey) {
        LOGGER.info("API key received for user {}.", apiKey.getUserId());
    }

    @Override
    public void submitLimitFok(AbstractTraderStrategy strategy, int userId, long orderId, String orderSize, String orderPrice, double orderPriceDouble, OrderSide orderSide) {
    }
}
