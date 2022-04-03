package cw.common.db.mysql;

import cw.common.event.IEvent;
import cwp.db.IDbEntity;

import javax.persistence.*;

@Entity
@Table(name = "api_keys")
@IdClass(ApiKeyId.class)
public class ApiKey implements IEvent, IDbEntity {
    @Id
    @Column(name = "user_id")
    private int userId;

    @Id
    @Column(name = "exchange_id")
    private byte exchange;

    @Column(name = "api_key")
    private String apiKey;

    @Column(name = "secret_key")
    private String secretKey;

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

    public String getApiKey() {
        return this.apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getSecretKey() {
        return this.secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    @Override
    public String toString() {
        return "ApiKey{" +
                "userId=" + userId +
                ", exchange=" + exchange +
                '}';
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
