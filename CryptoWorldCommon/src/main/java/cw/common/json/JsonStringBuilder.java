package cw.common.json;

/**
 * This interface presents a container of a string value the parser has parsed.
 * An instance of this container will be passed to
 * {@link JsonEventListener#onObjectMember(CharSequence)} or
 * {@link JsonEventListener#onStringValue(CharSequence)}.
 * Different implementations of the container may use specific optimizations.
 * For example:
 * 1. You parse a CharSequence, which contains the whole message. Then you could
 * use the Flyweight pattern to store a reference to the buffer and start
 * position of the value and its length. See {@link FlyweightStringBuilder}.
 * 2. When you parse a message part by part, you have to collect all chars of
 * current string value to prevent its corruption. This means
 * {@link CopyingStringBuilder} can be used.
 * 3. If you need to have the JSON values be unescaped, a copying builder should
 * be used instead of a flyweight one, since the length of the result string may
 * be different, less than original string's length. To unescape JSON values you
 * can use {@link CopyingStringBuilder#CopyingStringBuilder(boolean)} with true passed (default value).
 * @see FlyweightStringBuilder
 * @see CopyingStringBuilder
 */
public interface JsonStringBuilder extends CharSequence {
    /**
     * Notifies the builder that a text buffer which contains
     * a part of a JSON message is going to be parsed.
     *
     * @param data - text of JSON message
     * @param position - position of the opening quote
     */
    void start(CharSequence data, int position);

    /**
     * Appends next char to the current string value.
     * @param int - current char
     */
    void append(CharSequence data, int start, int len);

    /**
     * Appends '\' char to the current string value.
     */
    void appendEscape();

    void appendEscapedQuotationMark();

    void appendEscapedReverseSolidus();

    void appendEscapedSolidus();

    void appendEscapedBackspace();

    void appendEscapedFormfeed();

    void appendEscapedNewLine();

    void appendEscapedCarriageReturn();

    void appendEscapedHorisontalTab();

    void appendEscapedUnicodeU();

    boolean appendEscapedUnicodeChar1(char c);

    boolean appendEscapedUnicodeChar2(char c);

    boolean appendEscapedUnicodeChar3(char c);

    boolean appendEscapedUnicodeChar4(char c);
}
