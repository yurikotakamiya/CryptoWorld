package cw.feedhandler.binance;

import cw.common.json.JsonNumber;
import cw.common.json.JsonParserListenerAdaptor;

import java.util.Arrays;

public class BinanceQuoteJsonParserListener extends JsonParserListenerAdaptor {
    private static final String LAST_UPDATE_ID = "lastUpdateId";
    private static final String STREAM = "stream";
    private static final String BIDS = "bids";
    private static final String ASKS = "asks";

    private boolean foundLastUpdateId;
    private boolean[] foundBids;
    private boolean[] foundAsks;

    long lastUpdateId;
    StringBuilder stream;
    double bidPrice;
    double bidSize;
    double askPrice;
    double askSize;

    public BinanceQuoteJsonParserListener() {
        this.foundLastUpdateId = false;
        this.foundBids = new boolean[2];
        this.foundAsks = new boolean[2];

        this.stream = new StringBuilder();
    }

    @Override
    public boolean onObjectMember(CharSequence name) {
        int length = name.length();

        if ((STREAM.length() == length) && STREAM.equals(name.toString())) {
            this.stream.setLength(0);
        } else if ((LAST_UPDATE_ID.length() == length) && LAST_UPDATE_ID.equals(name.toString())) {
            this.foundLastUpdateId = true;
        } else if ((BIDS.length() == length) && BIDS.equals(name.toString())) {
            Arrays.fill(this.foundBids, true);
        } else if ((ASKS.length() == length) && ASKS.equals(name.toString())) {
            Arrays.fill(this.foundAsks, true);
        }

        return true;
    }

    @Override
    public boolean onNumberValue(JsonNumber number) {
        if (this.foundLastUpdateId) {
            this.lastUpdateId = number.mantissa();
            this.foundLastUpdateId = false;
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
        }

        return true;
    }
}
