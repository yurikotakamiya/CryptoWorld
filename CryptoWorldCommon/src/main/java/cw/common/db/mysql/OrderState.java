package cw.common.db.mysql;

public enum OrderState {
    NONE(false),
    SUBMIT(false),
    SUBMITTED(false),
    SUBMIT_REJECTED(true),
    CANCEL(false),
    CANCELED(true),
    PARTIAL_EXEC(false),
    EXECUTED(true);

    boolean isComplete;

    OrderState(boolean isComplete) {
        this.isComplete = isComplete;
    }

    public boolean isComplete() {
        return this.isComplete;
    }
}
