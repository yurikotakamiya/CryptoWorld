package cw.common.core;

import cw.common.db.mysql.Exchange;
import cw.common.db.mysql.TradingPair;

public interface ITraderStrategy {
    int getId();

    TradingPair getTradingPair();

    Exchange getExchange();

    void updateOrderClientOrderId(long id, String clientOrderId);
}
