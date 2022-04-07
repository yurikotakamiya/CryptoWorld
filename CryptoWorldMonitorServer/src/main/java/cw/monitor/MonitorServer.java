package cw.monitor;

import cw.common.db.mysql.MonitorConfig;
import cw.common.event.EventQueue;
import cw.common.event.IEventHandler;
import cw.common.server.AbstractServer;
import cw.common.timer.Timer;
import cw.common.timer.TimerQueue;
import cw.monitor.event.MonitorEventHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MonitorServer extends AbstractServer {
    private static final Logger LOGGER = LogManager.getLogger(MonitorServer.class.getSimpleName());

    private final IEventHandler eventHandler;

    public MonitorServer() throws Exception {
        this.eventHandler = new MonitorEventHandler(this.dbAdapter, this.timeManager, this::scheduleTimer);
        this.eventQueue = new EventQueue(this.eventHandler);
        this.timerQueue = new TimerQueue(this.timeManager, this.eventQueue::enqueue);

        loadConfigs();
    }

    private void loadConfigs() {
        this.dbAdapter.readAll(MonitorConfig.class).forEach(this.eventQueue::enqueue);
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
