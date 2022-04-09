package cw.common.event;

import cw.common.db.mysql.OrderAction;
import cw.common.db.mysql.OrderState;

import java.util.LinkedList;
import java.util.List;

public class OrderResponse implements IEvent {
    public int strategyId;
    public long orderId;
    public int userId;
    public String clientOrderId;
    public OrderAction orderAction;
    public OrderState orderState;
    public Double bidPrice;
    public Double askPrice;
    public long transactionTime;
    public List<Double> executedQuantities;
    public List<Double> executionPrice;

    public OrderResponse() {
        this.executedQuantities = new LinkedList<>();
        this.executionPrice = new LinkedList<>();
    }
}
