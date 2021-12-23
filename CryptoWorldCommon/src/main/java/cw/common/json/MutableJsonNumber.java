package cw.common.json;

public class MutableJsonNumber implements JsonNumber {
    private long mantissa;
    private int exp;

    public MutableJsonNumber() {
    }

    public MutableJsonNumber(final long mantissa, final int exp) {
        set(mantissa, exp);
    }

    public void set(final long mantissa, final int exp) {
        setMantissa(mantissa);
        setExp(exp);
    }

    public void setMantissa(final long mantissa) {
        this.mantissa = mantissa;
    }

    public void setExp(final int exp) {
        this.exp = exp;
    }

    @Override
    public long mantissa() {
        return mantissa;
    }

    @Override
    public int exp() {
        return exp;
    }

    public String toString() {
        return mantissa() + "E" + exp();
    }
}
