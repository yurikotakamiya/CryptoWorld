package cw.common.db.mysql;

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
