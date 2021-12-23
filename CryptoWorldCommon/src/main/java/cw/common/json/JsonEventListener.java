package cw.common.json;

public interface JsonEventListener {
    boolean onObjectStarted();

    boolean onObjectMember(CharSequence name);

    boolean onObjectEnded();

    boolean onArrayStarted();

    boolean onArrayEnded();

    boolean onStringValue(CharSequence data);

    boolean onNumberValue(JsonNumber number);

    boolean onTrueValue();

    boolean onFalseValue();

    boolean onNullValue();
}
