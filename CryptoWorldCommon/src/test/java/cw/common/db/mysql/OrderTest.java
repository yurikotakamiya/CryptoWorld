package cw.common.db.mysql;

import cwp.db.mysql.MySqlAdapter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Date;

public class OrderTest {
    @BeforeAll
    static void beforeAll() {
        HibernateUtil.setHibernateMapping();
    }

    @Test
    void test_Order() throws Exception {
        for (Order order : MySqlAdapter.getINSTANCE().readAll(Order.class)) {
            System.out.println(order);
        }
    }

    @Test
    void test_Insert() throws Exception {
        Order order = new Order();
        order.setId(920107L);
        order.setUserId(1);
        order.setStrategy((byte) 1);
        order.setTradingPair((byte) 1);
        order.setOrderSide((byte) 2);
        order.setOrderSize(0.5);
        order.setOrderPrice(0.2);
        order.setCreateTime(new Date());
        order.setUpdateTime(new Date());
        order.setOrderAction((byte) 4);
        order.setOrderState((byte) 5);
        order.setLeavesQuantity(0.3);
        order.setOrderType((byte) 2);
        order.setVersion(3);

        MySqlAdapter.getINSTANCE().write(order);
    }
}
