package cw.common.core.kucoin;

import cw.common.core.ExchangeApiHandler;
import cw.common.core.ITraderStrategy;
import cw.common.db.mysql.*;
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
    public void submitLimitFok(ITraderStrategy strategy, int userId, long orderId, String orderSize, String orderPrice, double orderPriceDouble, OrderSide orderSide) {
    }

    @Override
    public Object getHistoricalCandlestickBars(TradingPair tradingPair, CandlestickInterval interval) {
        return null;
    }
}
