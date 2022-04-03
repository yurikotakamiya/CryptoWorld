package cw.common.db.mysql;

import cwp.db.IDbEntity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "orders")
public class Order implements IDbEntity {
    @Id
    @Column(name = "order_id")
    private long id;

    @Column(name = "client_order_id")
    private String clientOrderId;

    @Column(name = "user_id")
    private int userId;

    @Column(name = "strategy_id")
    private byte strategy;

    @Column(name = "trading_pair_id")
    private byte tradingPair;

    @Column(name = "order_side_id")
    private byte orderSide;

    @Column(name = "order_type_id")
    private byte orderType;

    @Column(name = "order_time_in_force")
    private byte orderTimeInForce;

    @Column(name = "order_size")
    private double orderSize;

    @Column(name = "order_price")
    private double orderPrice;

    @Column(name = "create_time")
    private Date createTime;

    @Column(name = "update_time")
    private Date updateTime;

    @Column(name = "order_action_id")
    private byte orderAction;

    @Column(name = "order_state_id")
    private byte orderState;

    @Column(name = "order_leaves_quantity")
    private double leavesQuantity;

    @Column(name = "order_executed_quantity")
    private double executedQuantity;

    @Column(name = "version")
    private int version;

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getClientOrderId() {
        return this.clientOrderId;
    }

    public void setClientOrderId(String clientOrderId) {
        this.clientOrderId = clientOrderId;
    }

    public int getUserId() {
        return this.userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public byte getStrategy() {
        return this.strategy;
    }

    public void setStrategy(byte strategy) {
        this.strategy = strategy;
    }

    public byte getTradingPair() {
        return this.tradingPair;
    }

    public void setTradingPair(byte tradingPair) {
        this.tradingPair = tradingPair;
    }

    public byte getOrderSide() {
        return this.orderSide;
    }

    public void setOrderSide(byte orderSide) {
        this.orderSide = orderSide;
    }

    public double getOrderSize() {
        return this.orderSize;
    }

    public void setOrderSize(double orderSize) {
        this.orderSize = orderSize;
    }

    public double getOrderPrice() {
        return this.orderPrice;
    }

    public void setOrderPrice(double orderPrice) {
        this.orderPrice = orderPrice;
    }

    public Date getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return this.updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public byte getOrderAction() {
        return this.orderAction;
    }

    public void setOrderAction(byte orderAction) {
        this.orderAction = orderAction;
    }

    public byte getOrderState() {
        return this.orderState;
    }

    public void setOrderState(byte orderState) {
        this.orderState = orderState;
    }

    public double getLeavesQuantity() {
        return this.leavesQuantity;
    }

    public void setLeavesQuantity(double leavesQuantity) {
        this.leavesQuantity = leavesQuantity;
    }

    public byte getOrderType() {
        return this.orderType;
    }

    public void setOrderType(byte orderType) {
        this.orderType = orderType;
    }

    public byte getOrderTimeInForce() {
        return this.orderTimeInForce;
    }

    public void setOrderTimeInForce(byte orderTimeInForce) {
        this.orderTimeInForce = orderTimeInForce;
    }

    public double getExecutedQuantity() {
        return this.executedQuantity;
    }

    public void setExecutedQuantity(double executedQuantity) {
        this.executedQuantity = executedQuantity;
    }

    public int getVersion() {
        return this.version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", clientOrderId='" + clientOrderId + '\'' +
                ", userId=" + userId +
                ", strategy=" + strategy +
                ", tradingPair=" + tradingPair +
                ", orderSide=" + orderSide +
                ", orderType=" + orderType +
                ", orderTimeInForce=" + orderTimeInForce +
                ", orderSize=" + orderSize +
                ", orderPrice=" + orderPrice +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", orderAction=" + orderAction +
                ", orderState=" + orderState +
                ", leavesQuantity=" + leavesQuantity +
                ", executedQuantity=" + executedQuantity +
                ", version=" + version +
                '}';
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
