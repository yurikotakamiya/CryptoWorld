package cw.monitor.handler.binance;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.market.Candlestick;
import cw.common.core.binance.BinanceApiHandler;
import cw.common.db.mysql.CandlestickInterval;
import cw.common.db.mysql.TradingPair;

import java.util.List;

public class BinanceApiHandlerMock extends BinanceApiHandler {
    List<Candlestick> historicalCandlesticks;

    public void setHistoricalCandlesticks(List<Candlestick> historicalCandlesticks) {
        this.historicalCandlesticks = historicalCandlesticks;
    }

    @Override
    public Object getHistoricalCandlestickBars(TradingPair tradingPair, CandlestickInterval interval) {
        return this.historicalCandlesticks;
    }
}
