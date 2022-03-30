package cw.common.db.mysql;

import cwp.db.mysql.MySqlAdapter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TradeTest {
    @BeforeAll
    static void beforeAll() {
        HibernateUtil.setHibernateMapping();
    }

    @Test
    void test_Trade() throws Exception {
        for (Trade trade : MySqlAdapter.getINSTANCE().readAll(Trade.class)) {
            System.out.println(trade);
        }
    }
}
