package cw.common.db;

import cw.common.db.mysql.HibernateUtil;
import cw.common.db.mysql.Order;
import cwp.db.mysql.MySqlAdapter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

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
}
