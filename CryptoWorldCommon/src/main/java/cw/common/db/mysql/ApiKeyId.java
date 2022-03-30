package cw.common.db.mysql;

import java.io.Serializable;

public class ApiKeyId implements Serializable {
    private int userId;
    private byte exchange;

    public int getUserId() {
        return this.userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public byte getExchange() {
        return this.exchange;
    }

    public void setExchange(byte exchange) {
        this.exchange = exchange;
    }

    @Override
    public String toString() {
        return "ApiKeyId{" +
                "userId=" + userId +
                ", exchange=" + exchange +
                '}';
    }
}
