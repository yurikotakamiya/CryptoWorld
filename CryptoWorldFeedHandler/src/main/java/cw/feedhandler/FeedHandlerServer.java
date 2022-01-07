package cw.feedhandler;

import cw.feedhandler.binance.BinanceWebSocketMarketDataHandler;
import cw.feedhandler.ftx.FtxWebSocketMarketDataHandler;
import cw.feedhandler.kucoin.KucoinWebSocketMarketDataHandler;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FeedHandlerServer {
    private static final long TWELVE_HOURS_MILLIS = 12 * 60 * 60 * 1000;
    private final List<AbstractWebSocketMarketDataHandler> webSocketMarketDataHandlers;

    private FeedHandlerServer() throws Exception {
        this.webSocketMarketDataHandlers = new ArrayList<>();
        this.webSocketMarketDataHandlers.add(new BinanceWebSocketMarketDataHandler());
        this.webSocketMarketDataHandlers.add(new FtxWebSocketMarketDataHandler());
        this.webSocketMarketDataHandlers.add(new KucoinWebSocketMarketDataHandler());
    }

    private void connect() {
        for (AbstractWebSocketMarketDataHandler webSocketMarketDataHandler : this.webSocketMarketDataHandlers) {
            webSocketMarketDataHandler.connect();
        }
    }

    private void disconnect() {
        for (AbstractWebSocketMarketDataHandler webSocketMarketDataHandler : this.webSocketMarketDataHandlers) {
            webSocketMarketDataHandler.disconnect();
        }
    }

    private void scheduleTimer() {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                disconnect();
                connect();
            }
        };
        executorService.scheduleAtFixedRate(runnable, computeDelay(), TWELVE_HOURS_MILLIS, TimeUnit.MILLISECONDS);
    }

    private long computeDelay() {
        long currentTimeMillis = System.currentTimeMillis();
        Instant noon = OffsetDateTime.of(LocalDate.now(ZoneOffset.UTC), LocalTime.NOON, ZoneOffset.UTC).toInstant();
        long noonMillis = noon.toEpochMilli();
        long midnightMillis = noonMillis + TWELVE_HOURS_MILLIS;

        long diff = 0;
        if (noonMillis <= currentTimeMillis) {
            diff = midnightMillis - currentTimeMillis;
        } else if (noonMillis > currentTimeMillis) {
            diff = noonMillis - currentTimeMillis;
        }
        return diff;
    }

    public static void main(String[] args) throws Exception {
        FeedHandlerServer server = new FeedHandlerServer();
        server.connect();
        server.scheduleTimer();
    }
}
