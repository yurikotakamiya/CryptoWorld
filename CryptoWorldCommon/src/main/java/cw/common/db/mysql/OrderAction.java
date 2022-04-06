package cw.common.db.mysql;

public enum OrderAction {
    NONE,
    SUBMIT,
    SUBMITTED,
    SUBMIT_REJECTED,
    CANCEL,
    CANCELED,
    EXECUTED
}
