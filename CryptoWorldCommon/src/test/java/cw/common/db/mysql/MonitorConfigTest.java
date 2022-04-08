package cw.common.db.mysql;

import cwp.db.mysql.MySqlAdapter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class MonitorConfigTest {
    @BeforeAll
    static void beforeAll() {
        HibernateUtil.setHibernateMapping();
    }

    @Test
    void test_MonitorConfig() throws Exception {
        for (MonitorConfig monitorConfig : MySqlAdapter.getINSTANCE().readAll(MonitorConfig.class)) {
            System.out.println(monitorConfig);
        }
    }
}
