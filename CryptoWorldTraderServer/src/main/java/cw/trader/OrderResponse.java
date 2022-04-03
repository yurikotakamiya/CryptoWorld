package cw.trader;

import cw.common.event.IEvent;
import cw.common.order.OrderAction;
import cw.common.order.OrderState;

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
