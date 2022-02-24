package cw.trader.strategy.interval;

import cw.common.env.EnvUtil;
import cw.common.md.Exchange;
import cw.common.md.MarketDataType;
import cw.common.md.Quote;
import cw.common.md.TradingPair;
import cw.common.timer.Timer;
import cw.trader.ITradeHandler;
import cw.trader.handler.binance.BinanceTradeHandler;
import cw.trader.handler.ftx.FtxTradeHandler;
import cw.trader.handler.kucoin.KucoinTradeHandler;
import cw.trader.strategy.ITraderStrategy;
import cw.trader.strategy.TraderStrategyType;
import cwp.db.dynamodb.DynamoDbUtil;
import edu.emory.mathcs.backport.java.util.Collections;
import net.openhft.chronicle.map.ChronicleMap;
import net.openhft.chronicle.map.ChronicleMapBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Collection;

public class TraderIntervalStrategy implements ITraderStrategy {
    private static final Logger LOGGER = LogManager.getLogger(TraderIntervalStrategy.class.getSimpleName());

    private final Exchange exchange;
    private final TradingPair tradingPair;
    private final ChronicleMap<TradingPair, Quote> chronicleMap;
    private final Quote quote;

    private ITradeHandler handler;

    public TraderIntervalStrategy(Exchange exchange, TradingPair tradingPair) throws Exception {
        this.exchange = exchange;
        this.tradingPair = tradingPair;

        String marketDataMap = DynamoDbUtil.getMarketDataMap(this.exchange.getExchangeName(), EnvUtil.ENV.getEnvName());
        this.chronicleMap = ChronicleMapBuilder
                .of(TradingPair.class, Quote.class)
                .name(marketDataMap)
                .averageKey(this.tradingPair)
                .entries(10)
                .createPersistedTo(new File(marketDataMap));

        this.quote = Quote.getNativeObject();

        if (this.exchange == Exchange.BINANCE) {
            this.handler = new BinanceTradeHandler();
        } else if (this.exchange == Exchange.KUCOIN) {
            this.handler = new KucoinTradeHandler();
        } else if (this.exchange == Exchange.FTX) {
            this.handler = new FtxTradeHandler();
        }

        validate();
    }

    private void validate() throws Exception {
        if (this.handler == null) throw new Exception("No handler has been created.");
    }

    @Override
    public TraderStrategyType getTraderStrategyType() {
        return TraderStrategyType.INTERVAL;
    }

    @Override
    public void onTimerEvent(Timer timer) {
        this.chronicleMap.getUsing(this.tradingPair, this.quote);

        // TODO - perform actions
    }

    @Override
    public Exchange getExchange() {
        return this.exchange;
    }

    @Override
    public TradingPair getTradingPair() {
        return this.tradingPair;
    }

    @Override
    public Collection<MarketDataType> getInterestedMarketDataTypes() {
        return Collections.singleton(MarketDataType.QUOTE);
    }
}
