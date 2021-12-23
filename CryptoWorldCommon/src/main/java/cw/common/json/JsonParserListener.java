package cw.common.json;

public interface JsonParserListener extends JsonEventListener {
    void onJsonStarted();

    void onError(String error, int position);

    void onJsonEnded();
}
