package cw.common.json;

public final class FlyweightStringBuilder implements JsonStringBuilder {
    private CharSequence buffer;
    private int start;
    private int len;

    public FlyweightStringBuilder() {
    }

    public CharSequence buffer() {
        return buffer;
    }

    public int start() {
        return start;
    }

    @Override
    public int length() {
        return len;
    }

    @Override
    public char charAt(final int index) {
        return buffer.charAt(start + index);
    }

    @Override
    public void start(final CharSequence data, final int position) {
        buffer = data;
        start = position + 1;
        len = 0;
    }

    @Override
    public void append(final CharSequence data, final int start, final int len) {
        assert buffer == data;

        this.len += len;
    }

    @Override
    public void appendEscape() {
        len++;
    }

    @Override
    public void appendEscapedQuotationMark() {
        len++;
    }

    @Override
    public void appendEscapedReverseSolidus() {
        len++;
    }

    @Override
    public void appendEscapedSolidus() {
        len++;
    }

    @Override
    public void appendEscapedBackspace() {
        len++;
    }

    @Override
    public void appendEscapedFormfeed() {
        len++;
    }

    @Override
    public void appendEscapedNewLine() {
        len++;
    }

    @Override
    public void appendEscapedCarriageReturn() {
        len++;
    }

    @Override
    public void appendEscapedHorisontalTab() {
        len++;
    }

    @Override
    public void appendEscapedUnicodeU() {
        len++;
    }

    @Override
    public boolean appendEscapedUnicodeChar1(final char c) {
        return appendUnicodeChar(c);
    }

    @Override
    public boolean appendEscapedUnicodeChar2(final char c) {
        return appendUnicodeChar(c);
    }

    @Override
    public boolean appendEscapedUnicodeChar3(final char c) {
        return appendUnicodeChar(c);
    }

    @Override
    public boolean appendEscapedUnicodeChar4(final char c) {
        return appendUnicodeChar(c);
    }

    @Override
    public CharSequence subSequence(final int start, final int end) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        if (buffer == null) {
            return "null";
        }
        if (len == 0) {
            return "";
        }
        return new StringBuilder().append(buffer, start, start + len).toString();
    }

    private boolean appendUnicodeChar(final char c) {
        switch (c) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
            case 'A':
            case 'B':
            case 'C':
            case 'D':
            case 'E':
            case 'F':
            case 'a':
            case 'b':
            case 'c':
            case 'd':
            case 'e':
            case 'f':
                len++;
                return true;
            default:
                break;
        }
        return false;
    }
}
