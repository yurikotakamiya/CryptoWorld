package cw.common.db.mysql;

import cwp.db.mysql.MySqlAdapter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class StrategyConfigTest {
    @BeforeAll
    static void beforeAll() {
        HibernateUtil.setHibernateMapping();
    }

    @Test
    void test_StrategyConfig() throws Exception {
        for (StrategyConfig strategyConfig : MySqlAdapter.getINSTANCE().readAll(StrategyConfig.class)) {
            System.out.println(strategyConfig);
        }
    }
}
