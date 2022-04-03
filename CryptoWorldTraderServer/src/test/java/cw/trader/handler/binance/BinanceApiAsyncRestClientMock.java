package cw.trader.handler.binance;

import com.binance.api.client.BinanceApiAsyncRestClient;
import com.binance.api.client.BinanceApiCallback;
import com.binance.api.client.domain.account.*;
import com.binance.api.client.domain.account.request.*;
import com.binance.api.client.domain.event.ListenKey;
import com.binance.api.client.domain.general.Asset;
import com.binance.api.client.domain.general.ExchangeInfo;
import com.binance.api.client.domain.general.ServerTime;
import com.binance.api.client.domain.market.*;

import java.util.LinkedList;
import java.util.List;

public class BinanceApiAsyncRestClientMock implements BinanceApiAsyncRestClient {
    public List<NewOrderResponse> newOrderResponses;
    public BinanceApiCallback<NewOrderResponse> binanceApiCallback;

    public BinanceApiAsyncRestClientMock() {
        this.newOrderResponses = new LinkedList<>();
    }

    @Override
    public void ping(BinanceApiCallback<Void> binanceApiCallback) {
    }

    @Override
    public void getServerTime(BinanceApiCallback<ServerTime> binanceApiCallback) {
    }

    @Override
    public void getExchangeInfo(BinanceApiCallback<ExchangeInfo> binanceApiCallback) {
    }

    @Override
    public void getAllAssets(BinanceApiCallback<List<Asset>> binanceApiCallback) {
    }

    @Override
    public void getOrderBook(String s, Integer integer, BinanceApiCallback<OrderBook> binanceApiCallback) {
    }

    @Override
    public void getTrades(String s, Integer integer, BinanceApiCallback<List<TradeHistoryItem>> binanceApiCallback) {
    }

    @Override
    public void getHistoricalTrades(String s, Integer integer, Long aLong, BinanceApiCallback<List<TradeHistoryItem>> binanceApiCallback) {
    }

    @Override
    public void getAggTrades(String s, String s1, Integer integer, Long aLong, Long aLong1, BinanceApiCallback<List<AggTrade>> binanceApiCallback) {
    }

    @Override
    public void getAggTrades(String s, BinanceApiCallback<List<AggTrade>> binanceApiCallback) {
    }

    @Override
    public void getCandlestickBars(String s, CandlestickInterval candlestickInterval, Integer integer, Long aLong, Long aLong1, BinanceApiCallback<List<Candlestick>> binanceApiCallback) {
    }

    @Override
    public void getCandlestickBars(String s, CandlestickInterval candlestickInterval, BinanceApiCallback<List<Candlestick>> binanceApiCallback) {
    }

    @Override
    public void get24HrPriceStatistics(String s, BinanceApiCallback<TickerStatistics> binanceApiCallback) {
    }

    @Override
    public void getAll24HrPriceStatistics(BinanceApiCallback<List<TickerStatistics>> binanceApiCallback) {
    }

    @Override
    public void getAllPrices(BinanceApiCallback<List<TickerPrice>> binanceApiCallback) {
    }

    @Override
    public void getPrice(String s, BinanceApiCallback<TickerPrice> binanceApiCallback) {
    }

    @Override
    public void getBookTickers(BinanceApiCallback<List<BookTicker>> binanceApiCallback) {
    }

    @Override
    public void newOrder(NewOrder newOrder, BinanceApiCallback<NewOrderResponse> binanceApiCallback) {
        this.binanceApiCallback = binanceApiCallback;

        while (!this.newOrderResponses.isEmpty()) {
            this.binanceApiCallback.onResponse(this.newOrderResponses.remove(0));
        }
    }

    public void flushNewOrderResponses() {
        if (this.binanceApiCallback == null) return;

        while (!this.newOrderResponses.isEmpty()) {
            this.binanceApiCallback.onResponse(this.newOrderResponses.remove(0));
        }
    }

    @Override
    public void newOrderTest(NewOrder newOrder, BinanceApiCallback<Void> binanceApiCallback) {
    }

    @Override
    public void getOrderStatus(OrderStatusRequest orderStatusRequest, BinanceApiCallback<Order> binanceApiCallback) {
    }

    @Override
    public void cancelOrder(CancelOrderRequest cancelOrderRequest, BinanceApiCallback<CancelOrderResponse> binanceApiCallback) {
    }

    @Override
    public void getOpenOrders(OrderRequest orderRequest, BinanceApiCallback<List<Order>> binanceApiCallback) {
    }

    @Override
    public void getAllOrders(AllOrdersRequest allOrdersRequest, BinanceApiCallback<List<Order>> binanceApiCallback) {
    }

    @Override
    public void getAccount(Long aLong, Long aLong1, BinanceApiCallback<Account> binanceApiCallback) {
    }

    @Override
    public void getAccount(BinanceApiCallback<Account> binanceApiCallback) {
    }

    @Override
    public void getMyTrades(String s, Integer integer, Long aLong, Long aLong1, Long aLong2, BinanceApiCallback<List<Trade>> binanceApiCallback) {
    }

    @Override
    public void getMyTrades(String s, Integer integer, BinanceApiCallback<List<Trade>> binanceApiCallback) {
    }

    @Override
    public void getMyTrades(String s, BinanceApiCallback<List<Trade>> binanceApiCallback) {
    }

    @Override
    public void withdraw(String s, String s1, String s2, String s3, String s4, BinanceApiCallback<WithdrawResult> binanceApiCallback) {
    }

    @Override
    public void getDepositHistory(String s, BinanceApiCallback<DepositHistory> binanceApiCallback) {
    }

    @Override
    public void getWithdrawHistory(String s, BinanceApiCallback<WithdrawHistory> binanceApiCallback) {
    }

    @Override
    public void getDepositAddress(String s, BinanceApiCallback<DepositAddress> binanceApiCallback) {
    }

    @Override
    public void startUserDataStream(BinanceApiCallback<ListenKey> binanceApiCallback) {
    }

    @Override
    public void keepAliveUserDataStream(String s, BinanceApiCallback<Void> binanceApiCallback) {
    }

    @Override
    public void closeUserDataStream(String s, BinanceApiCallback<Void> binanceApiCallback) {
    }
}
