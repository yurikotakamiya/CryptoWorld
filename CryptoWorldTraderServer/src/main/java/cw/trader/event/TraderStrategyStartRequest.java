package cw.trader.event;

import cw.common.event.IEvent;
import cw.common.md.Exchange;
import cw.common.md.TradingPair;
import cw.trader.strategy.TraderStrategyType;

public class TraderStrategyStartRequest implements IEvent {
    TraderStrategyType strategyType;
    Exchange exchange;
    TradingPair tradingPair;

    public TraderStrategyStartRequest(TraderStrategyType strategyType, Exchange exchange, TradingPair tradingPair) {
        this.strategyType = strategyType;
        this.exchange = exchange;
        this.tradingPair = tradingPair;
    }
}
