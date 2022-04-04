package cw.common.timer;

public class TimeManager implements ITimeManager {
    public static final long ONE_SEC = 1000;

    @Override
    public long getCurrentTimeMillis() {
        return System.currentTimeMillis();
    }
}
