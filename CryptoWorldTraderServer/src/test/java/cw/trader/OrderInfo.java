package cw.trader;

import cw.common.db.mysql.OrderSide;

public class OrderInfo {
    public int userId;
    public String orderSize;
    public double orderPrice;
    public OrderSide orderSide;
}
