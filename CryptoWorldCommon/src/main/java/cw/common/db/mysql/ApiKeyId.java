package cw.common.db.mysql;

import java.io.Serializable;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApiKeyId apiKeyId = (ApiKeyId) o;
        return userId == apiKeyId.userId && exchange == apiKeyId.exchange;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, exchange);
    }
}
