package cw.trader.handler.binance;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.binance.api.client.domain.market.Candlestick;
import com.binance.api.client.domain.market.CandlestickInterval;
import cw.common.db.mysql.Exchange;
import cw.common.db.mysql.TradingPair;

import java.util.List;

public class BinanceApiClientTest {
    public static void main(String[] args) {
        BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance(null, null);
        BinanceApiRestClient client = factory.newRestClient();

        List<Candlestick> candlesticks = client.getCandlestickBars(TradingPair.ETHUSDT.getExchangeToSymbolMap().get(Exchange.BINANCE), CandlestickInterval.DAILY, 10, null, null);

        for (Candlestick candlestick : candlesticks) {
            System.out.println(candlestick);
        }
    }
}
