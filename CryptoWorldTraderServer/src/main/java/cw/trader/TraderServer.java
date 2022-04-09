package cw.trader;

import cw.common.core.AbstractServer;
import cw.common.core.ExchangeApiHandler;
import cw.common.core.binance.BinanceApiHandler;
import cw.common.core.ftx.FtxApiHandler;
import cw.common.core.kucoin.KucoinApiHandler;
import cw.common.db.mysql.ApiKey;
import cw.common.db.mysql.Exchange;
import cw.common.db.mysql.StrategyConfig;
import cw.common.event.EventQueue;
import cw.common.event.IEventHandler;
import cw.common.timer.Timer;
import cw.common.timer.TimerQueue;
import cw.trader.event.TraderEventHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class TraderServer extends AbstractServer {
    private static final Logger LOGGER = LogManager.getLogger(TraderServer.class.getSimpleName());

    private final Map<Exchange, ExchangeApiHandler> exchangeApiHandlers;
    private final IEventHandler eventHandler;

    TraderServer() throws Exception {
        this.exchangeApiHandlers = generateExchangeApiHandlers();
        this.eventHandler = new TraderEventHandler(this.exchangeApiHandlers, this.dbAdapter, this.timeManager, this::scheduleTimer);
        this.eventQueue = new EventQueue(this.eventHandler);
        this.timerQueue = new TimerQueue(this.timeManager, this.eventQueue::enqueue);

        this.exchangeApiHandlers.values().forEach(e -> e.setEnqueueCallback(this.eventQueue::enqueue));
        loadConfigs();
    }

    @Override
    protected Map<Exchange, ExchangeApiHandler> generateExchangeApiHandlers() {
        Map<Exchange, ExchangeApiHandler> map = new HashMap<>();

        ExchangeApiHandler handler = new BinanceApiHandler();
        map.put(handler.getExchange(), handler);

        handler = new FtxApiHandler();
        map.put(handler.getExchange(), handler);

        handler = new KucoinApiHandler();
        map.put(handler.getExchange(), handler);

        return map;
    }

    private void loadConfigs() {
        this.dbAdapter.readAll(ApiKey.class).forEach(this.eventQueue::enqueue);
        this.dbAdapter.readAll(StrategyConfig.class).forEach(this.eventQueue::enqueue);
    }

    private void scheduleTimer(Timer timer) {
        this.timerQueue.scheduleTimer(timer);
    }

    private void start() {
        new Thread(this.eventQueue).start();
        new Thread(this.timerQueue).start();
    }

    public static void main(String[] args) throws Exception {
        new TraderServer().start();
        LOGGER.info("Server started.");
    }
}
