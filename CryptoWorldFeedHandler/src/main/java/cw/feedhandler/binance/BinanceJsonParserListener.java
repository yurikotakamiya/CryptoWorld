package cw.feedhandler.binance;

import cw.common.json.JsonNumber;
import cw.common.json.JsonParserListenerAdaptor;

import java.util.Arrays;

public class BinanceJsonParserListener extends JsonParserListenerAdaptor {
    private static final String LAST_UPDATE_ID = "lastUpdateId";
    private static final String STREAM = "stream";

    // Quote
    private static final String BIDS = "bids";
    private static final String ASKS = "asks";

    // Candlestick
    private static final String INTERVAL = "i";
    private static final String OPEN_TIME = "t";
    private static final String CLOSE_TIME = "T";
    private static final String OPEN_PRICE = "o";
    private static final String CLOSE_PRICE = "c";

    private boolean foundLastUpdateId;
    private boolean[] foundBids;
    private boolean[] foundAsks;
    private boolean foundInterval;
    private boolean foundOpenTime;
    private boolean foundCloseTime;
    private boolean foundOpenPrice;
    private boolean foundClosePrice;

    long lastUpdateId;
    StringBuilder stream;

    // Quote
    double bidPrice;
    double bidSize;
    double askPrice;
    double askSize;

    // Candlestick
    String interval;
    long openTime;
    long closeTime;
    double openPrice;
    double closePrice;

    public BinanceJsonParserListener() {
        this.foundLastUpdateId = false;
        this.foundBids = new boolean[2];
        this.foundAsks = new boolean[2];

        this.stream = new StringBuilder();
    }

    @Override
    public boolean onObjectMember(CharSequence name) {
        String nameStr = name.toString();

        if (STREAM.equals(nameStr)) {
            this.stream.setLength(0);
        } else if (LAST_UPDATE_ID.equals(nameStr)) {
            this.foundLastUpdateId = true;
        } else if (BIDS.equals(nameStr)) {
            Arrays.fill(this.foundBids, true);
        } else if (ASKS.equals(nameStr)) {
            Arrays.fill(this.foundAsks, true);
        } else if (INTERVAL.equals(nameStr)) {
            this.foundInterval = true;
        } else if (OPEN_TIME.equals(nameStr)) {
            this.foundOpenTime = true;
        } else if (CLOSE_TIME.equals(nameStr)) {
            this.foundCloseTime = true;
        } else if (OPEN_PRICE.equals(nameStr)) {
            this.foundOpenPrice = true;
        } else if (CLOSE_PRICE.equals(nameStr)) {
            this.foundClosePrice = true;
        }

        return true;
    }

    @Override
    public boolean onNumberValue(JsonNumber number) {
        if (this.foundLastUpdateId) {
            this.lastUpdateId = number.mantissa();
            this.foundLastUpdateId = false;
        } else if (this.foundOpenTime) {
            this.openTime = number.mantissa();
            this.foundOpenTime = false;
        } else if (this.foundCloseTime) {
            this.closeTime = number.mantissa();
            this.foundCloseTime = false;
        }

        return true;
    }

    @Override
    public boolean onStringValue(CharSequence data) {
        if (this.stream.length() == 0) {
            this.stream.append(data);
        } else if (this.foundBids[0]) {
            this.bidPrice = Double.parseDouble(data.toString());
            this.foundBids[0] = false;
        } else if (this.foundBids[1]) {
            this.bidSize = Double.parseDouble(data.toString());
            this.foundBids[1] = false;
        } else if (this.foundAsks[0]) {
            this.askPrice = Double.parseDouble(data.toString());
            this.foundAsks[0] = false;
        } else if (this.foundAsks[1]) {
            this.askSize = Double.parseDouble(data.toString());
            this.foundAsks[1] = false;
        } else if (this.foundInterval) {
            this.interval = data.toString();
            this.foundInterval = false;
        } else if (this.foundOpenPrice) {
            this.openPrice = Double.parseDouble(data.toString());
            this.foundOpenPrice = false;
        } else if (this.foundClosePrice) {
            this.closePrice = Double.parseDouble(data.toString());
            this.foundClosePrice = false;
        }

        return true;
    }
}
