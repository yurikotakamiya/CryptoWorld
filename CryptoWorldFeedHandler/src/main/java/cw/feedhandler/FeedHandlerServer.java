package cw.feedhandler;

import cw.common.db.mysql.Exchange;
import cw.common.db.mysql.HibernateUtil;
import cw.common.db.mysql.MonitorConfig;
import cw.common.db.mysql.StrategyConfig;
import cw.feedhandler.binance.BinanceWebSocketMarketDataHandler;
import cw.feedhandler.ftx.FtxWebSocketMarketDataHandler;
import cw.feedhandler.kucoin.KucoinWebSocketMarketDataHandler;
import cwp.db.IDbEntity;
import cwp.db.mysql.MySqlAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FeedHandlerServer {
    private static final Logger LOGGER = LogManager.getLogger(FeedHandlerServer.class.getSimpleName());
    private static final long TWELVE_HOURS_MILLIS = 12 * 60 * 60 * 1000;

    private final MySqlAdapter dbAdapter;
    private final Set<Exchange> interestedMarketData;

    private List<AbstractWebSocketMarketDataHandler> webSocketMarketDataHandlers;

    private FeedHandlerServer() throws Exception {
        this.dbAdapter = getMySqlAdapter();
        this.interestedMarketData = new HashSet<>();

        loadConfigs();
        generateMarketDataHandlers();
    }

    private MySqlAdapter getMySqlAdapter() throws Exception {
        HibernateUtil.setHibernateMapping();
        return MySqlAdapter.getINSTANCE();
    }

    private void loadConfigs() {
        this.dbAdapter.readAll(StrategyConfig.class).forEach(this::handleDbEntity);
        this.dbAdapter.readAll(MonitorConfig.class).forEach(this::handleDbEntity);
    }

    private void handleDbEntity(IDbEntity dbEntity) {
        if (dbEntity instanceof StrategyConfig) {
            StrategyConfig strategyConfig = (StrategyConfig) dbEntity;
            Exchange exchange = Exchange.values()[strategyConfig.getExchange()];

            this.interestedMarketData.add(exchange);
        } else if (dbEntity instanceof MonitorConfig) {
            MonitorConfig monitorConfig = (MonitorConfig) dbEntity;
            Exchange exchange = Exchange.values()[monitorConfig.getExchange()];

            this.interestedMarketData.add(exchange);
        } else {
            LOGGER.error("Received unknown DB entity {}", dbEntity);
        }
    }

    private void generateMarketDataHandlers() throws Exception {
        this.webSocketMarketDataHandlers = new ArrayList<>();

        for (Exchange exchange : this.interestedMarketData) {
            if (exchange == Exchange.BINANCE) {
                this.webSocketMarketDataHandlers.add(new BinanceWebSocketMarketDataHandler());
            } else if (exchange == Exchange.FTX) {
                this.webSocketMarketDataHandlers.add(new FtxWebSocketMarketDataHandler());
            } else if (exchange == Exchange.KUCOIN) {
                this.webSocketMarketDataHandlers.add(new KucoinWebSocketMarketDataHandler());
            }
        }
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
