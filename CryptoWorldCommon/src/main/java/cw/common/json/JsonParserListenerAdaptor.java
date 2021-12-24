package cw.common.json;

public class JsonParserListenerAdaptor implements JsonParserListener {
    @Override
    public void onJsonStarted() {
    }

    @Override
    public void onError(final String error, final int position) {
    }

    @Override
    public void onJsonEnded() {
    }

    @Override
    public boolean onObjectStarted() {
        return true;
    }

    @Override
    public boolean onObjectMember(final CharSequence name) {
        return true;
    }

    @Override
    public boolean onObjectEnded() {
        return true;
    }

    @Override
    public boolean onArrayStarted() {
        return true;
    }

    @Override
    public boolean onArrayEnded() {
        return true;
    }

    @Override
    public boolean onStringValue(final CharSequence data) {
        return true;
    }

    @Override
    public boolean onNumberValue(final JsonNumber number) {
        return true;
    }

    @Override
    public boolean onTrueValue() {
        return true;
    }

    @Override
    public boolean onFalseValue() {
        return true;
    }

    @Override
    public boolean onNullValue() {
        return true;
    }
}
