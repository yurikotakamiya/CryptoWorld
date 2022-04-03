package cw.common.timer;

public class TimeManager implements ITimeManager {
    @Override
    public long getCurrentTimeMillis() {
        return System.currentTimeMillis();
    }
}
