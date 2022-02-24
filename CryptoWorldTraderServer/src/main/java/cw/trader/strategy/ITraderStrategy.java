package cw.trader.strategy;

import cw.common.md.Exchange;
import cw.common.md.MarketDataType;
import cw.common.md.TradingPair;
import cw.common.timer.Timer;

import java.util.Collection;

public interface ITraderStrategy {
    TraderStrategyType getTraderStrategyType();

    void onTimerEvent(Timer timer);

    Exchange getExchange();

    TradingPair getTradingPair();

    Collection<MarketDataType> getInterestedMarketDataTypes();
}
