package cw.common.db.mysql;

import java.util.HashMap;
import java.util.Map;

public enum CandlestickInterval {
    NONE("NONE"),
    ONE_MINUTE("1m"),
    THREE_MINUTES("3m"),
    FIVE_MINUTES("5m"),
    FIFTEEN_MINUTES("15m"),
    HALF_HOURLY("30m"),
    HOURLY("1h"),
    TWO_HOURLY("2h"),
    FOUR_HOURLY("4h"),
    SIX_HOURLY("6h"),
    EIGHT_HOURLY("8h"),
    TWELVE_HOURLY("12h"),
    DAILY("1d"),
    THREE_DAILY("3d"),
    WEEKLY("1w"),
    MONTHLY("1M");

    public static final Map<String, CandlestickInterval> INTERVALS_BY_DESCRIPTION;

    static {
        INTERVALS_BY_DESCRIPTION = new HashMap<>();

        for (CandlestickInterval interval : CandlestickInterval.values()) {
            INTERVALS_BY_DESCRIPTION.put(interval.interval, interval);
        }
    }

    private String interval;

    private CandlestickInterval(String interval) {
        this.interval = interval;
    }

    public String getInterval() {
        return this.interval;
    }
}
