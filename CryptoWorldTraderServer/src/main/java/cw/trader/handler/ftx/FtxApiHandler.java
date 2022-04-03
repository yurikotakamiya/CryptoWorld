package cw.trader.handler.ftx;

import cw.common.db.mysql.ApiKey;
import cw.common.md.Exchange;
import cw.common.order.OrderSide;
import cw.trader.ExchangeApiHandler;
import cw.trader.strategy.AbstractTraderStrategy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FtxApiHandler extends ExchangeApiHandler {
    private static final Logger LOGGER = LogManager.getLogger(FtxApiHandler.class.getSimpleName());

    @Override
    public Exchange getExchange() {
        return Exchange.FTX;
    }

    @Override
    public void add(ApiKey apiKey) {
        LOGGER.info("API key received for user {}.", apiKey.getUserId());
    }

    @Override
    public void submitLimitFok(AbstractTraderStrategy strategy, int userId, long orderId, String orderSize, String orderPrice, double orderPriceDouble, OrderSide orderSide) {
    }
}
