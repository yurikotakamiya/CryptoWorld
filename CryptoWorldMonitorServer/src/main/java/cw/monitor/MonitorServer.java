package cw.monitor;

import cw.common.core.AbstractServer;
import cw.common.core.ExchangeApiHandler;
import cw.common.core.binance.BinanceApiHandler;
import cw.common.core.ftx.FtxApiHandler;
import cw.common.core.kucoin.KucoinApiHandler;
import cw.common.db.mysql.Exchange;
import cw.common.db.mysql.MonitorConfig;
import cw.common.db.mysql.User;
import cw.common.event.EventQueue;
import cw.common.event.IEventHandler;
import cw.common.timer.Timer;
import cw.common.timer.TimerQueue;
import cw.monitor.event.MonitorEventHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class MonitorServer extends AbstractServer {
    private static final Logger LOGGER = LogManager.getLogger(MonitorServer.class.getSimpleName());

    private final Map<Exchange, ExchangeApiHandler> exchangeApiHandlers;
    private final IEventHandler eventHandler;

    public MonitorServer() throws Exception {
        this.exchangeApiHandlers = generateExchangeApiHandlers();
        this.eventHandler = new MonitorEventHandler(this.exchangeApiHandlers, this.dbAdapter, this.timeManager, this::scheduleTimer);
        this.eventQueue = new EventQueue(this.eventHandler);
        this.timerQueue = new TimerQueue(this.timeManager, this.eventQueue::enqueue);

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
        this.dbAdapter.readAll(MonitorConfig.class).forEach(this.eventQueue::enqueue);
        this.dbAdapter.readAll(User.class).forEach(this.eventQueue::enqueue);
    }

    private void scheduleTimer(Timer timer) {
        this.timerQueue.scheduleTimer(timer);
    }

    private void start() {
        new Thread(this.eventQueue).start();
        new Thread(this.timerQueue).start();
    }

    public static void main(String[] args) throws Exception {
        new MonitorServer().start();
        LOGGER.info("Server started.");
    }
}
