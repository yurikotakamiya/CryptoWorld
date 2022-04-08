package cw.feedhandler.kucoin;

import cw.common.json.JsonNumber;
import cw.common.json.JsonParserListenerAdaptor;

public class KucoinJsonParserListener extends JsonParserListenerAdaptor {
    private static final String BID = "bestBid";
    private static final String ASK = "bestAsk";
    private static final String BID_SIZE = "bestBidSize";
    private static final String ASK_SIZE = "bestAskSize";
    private static final String TIME = "time";
    private static final String MARKET = "topic";

    private boolean foundBid;
    private boolean foundAsk;
    private boolean foundBidSize;
    private boolean foundAskSize;
    private boolean foundTime;
    private boolean foundMarket;

    String market;
    double bid;
    double bidSize;
    double ask;
    double askSize;
    long time;

    @Override
    public boolean onObjectMember(CharSequence name) {
        String nameStr = name.toString();
        if (MARKET.equals(nameStr)) {
            this.foundMarket = true;
        } else if (TIME.equals(nameStr)) {
            this.foundTime = true;
        } else if (BID.equals(nameStr)) {
            this.foundBid = true;
        } else if (ASK.equals(nameStr)) {
            this.foundAsk = true;
        } else if (BID_SIZE.equals(nameStr)) {
            this.foundBidSize = true;
        } else if (ASK_SIZE.equals(nameStr)) {
            this.foundAskSize = true;
        }
        return true;
    }

    @Override
    public boolean onStringValue(CharSequence data) {
        if (foundMarket) {
            this.market = data.toString();
            this.foundMarket = false;
        } else if (this.foundBid) {
            this.bid = Double.parseDouble(data.toString());
            this.foundBid = false;
        } else if (this.foundAsk) {
            this.ask = Double.parseDouble(data.toString());
            this.foundAsk = false;
        } else if (this.foundBidSize) {
            this.bidSize = Double.parseDouble(data.toString());
            this.foundBidSize = false;
        } else if (this.foundAskSize) {
            this.askSize = Double.parseDouble(data.toString());
            this.foundAskSize = false;
        }
        return true;
    }

    @Override
    public boolean onNumberValue(JsonNumber number) {
        if (this.foundTime) {
            this.time = (long) (number.mantissa() * Math.pow(10, number.exp()));
            this.foundTime = false;
        }
        return true;
    }
}
