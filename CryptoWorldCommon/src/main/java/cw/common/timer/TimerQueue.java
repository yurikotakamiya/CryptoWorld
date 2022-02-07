package cw.common.timer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;

public class TimerQueue implements Runnable {
    private static final Logger LOGGER = LogManager.getLogger(TimerQueue.class.getSimpleName());

    private final PriorityBlockingQueue<Timer> priorityQueue;

    public TimerQueue() {
        this.priorityQueue = new PriorityBlockingQueue<>(100, new Comparator<Timer>() {
            @Override
            public int compare(Timer o1, Timer o2) {
                return (int) (o1.expirationTime - o2.expirationTime);
            }
        });
    }

    @Override
    public void run() {
        while (true) {
            try {
                Timer timer = this.priorityQueue.poll();

                if (timer != null) {
                    // TODO - take action
                }
            } catch (Exception e) {
                LOGGER.error("Error occurred while processing timer.", e);
            }
        }
    }

    private void schedule(Timer timer) {
        this.priorityQueue.add(timer);
    }
}
