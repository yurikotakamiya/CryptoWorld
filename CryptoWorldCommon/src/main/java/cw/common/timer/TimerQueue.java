package cw.common.timer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.function.Consumer;

public class TimerQueue implements Runnable {
    private static final Logger LOGGER = LogManager.getLogger(TimerQueue.class.getSimpleName());

    private final PriorityBlockingQueue<Timer> priorityQueue;
    private final ITimeManager timeManager;
    private final Consumer<Timer> timerConsumer;

    public TimerQueue(ITimeManager timeManager, Consumer<Timer> timerConsumer) {
        this.priorityQueue = new PriorityBlockingQueue<>(100, (o1, o2) -> (int) (o1.expirationTime - o2.expirationTime));
        this.timeManager = timeManager;
        this.timerConsumer = timerConsumer;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Timer timer = this.priorityQueue.peek();

                if ((timer != null) && (this.timeManager.getCurrentTimeMillis() >= timer.expirationTime)) {
                    this.priorityQueue.poll();
                    this.timerConsumer.accept(timer);
                }
            } catch (Exception e) {
                LOGGER.error("Error occurred while processing timer.", e);
            }
        }
    }

    public void scheduleTimer(Timer timer) {
        this.priorityQueue.add(timer);
    }
}
