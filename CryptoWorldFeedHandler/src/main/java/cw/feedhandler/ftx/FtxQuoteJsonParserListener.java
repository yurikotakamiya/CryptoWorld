package cw.feedhandler.ftx;

import cw.common.json.JsonNumber;
import cw.common.json.JsonParserListenerAdaptor;

public class FtxQuoteJsonParserListener extends JsonParserListenerAdaptor {
    private static final String MARKET = "market";
    private static final String BID = "bid";
    private static final String BID_SIZE = "bidSize";
    private static final String ASK = "ask";
    private static final String ASK_SIZE = "askSize";
    private static final String TIME = "time";

    private boolean foundMarket;
    private boolean foundBid;
    private boolean foundAsk;
    private boolean foundBidSize;
    private boolean foundAskSize;
    private boolean foundTime;

    String market;
    double bidPrice;
    double bidSize;
    double askPrice;
    double askSize;
    long time;

    @Override
    public boolean onObjectMember(CharSequence name) {
        if (MARKET.equals(name.toString())) {
            foundMarket = true;
        } else if (TIME.equals(name.toString())) {
            foundTime = true;
        } else if (BID.equals(name.toString())) {
            foundBid = true;
        } else if (ASK.equals(name.toString())) {
            foundAsk = true;
        } else if (BID_SIZE.equals(name.toString())) {
            foundBidSize = true;
        } else if (ASK_SIZE.equals(name.toString())) {
            foundAskSize = true;
        }
        return true;
    }

    @Override
    public boolean onNumberValue(JsonNumber number) {
        if (this.foundTime) {
            this.time = (long) (number.mantissa() * Math.pow(10, number.exp()));
            this.foundTime = false;
        } else if (this.foundBid) {
            this.bidPrice = number.mantissa() / Math.pow(10, -number.exp());
            this.foundBid = false;
        } else if (this.foundAsk) {
            this.askPrice = number.mantissa() / Math.pow(10, -number.exp());
            this.foundAsk = false;
        } else if (this.foundBidSize) {
            this.bidSize = number.mantissa() / Math.pow(10, -number.exp());
            this.foundBidSize = false;
        } else if (this.foundAskSize) {
            this.askSize = number.mantissa() / Math.pow(10, -number.exp());
            this.foundAskSize = false;
        }
        return true;
    }

    @Override
    public boolean onStringValue(CharSequence data) {
        if (foundMarket) {
            market = data.toString();
            foundMarket = false;
        }
        return true;
    }
}
