package cw.common.event;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ConcurrentLinkedQueue;

public class EventQueue implements Runnable {
    private static final Logger LOGGER = LogManager.getLogger(EventQueue.class.getSimpleName());

    private final IEventHandler eventHandler;
    private final ConcurrentLinkedQueue<IEvent> queue;

    public EventQueue(IEventHandler eventHandler) {
        this.eventHandler = eventHandler;
        this.queue = new ConcurrentLinkedQueue<>();
    }

    public void enqueue(IEvent event) {
        this.queue.add(event);
    }

    @Override
    public void run() {
        while (true) {
            try {
                IEvent event = this.queue.poll();

                if (event != null) {
                    this.eventHandler.process(event);
                }
            } catch (Exception e) {
                LOGGER.error("Error occurred while processing event.", e);
            }
        }
    }
}
