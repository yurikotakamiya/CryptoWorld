package cw.common.timer;

public class RealTimeManager implements ITimeManager {
    @Override
    public long getCurrentTimeMillis() {
        return System.currentTimeMillis();
    }
}
