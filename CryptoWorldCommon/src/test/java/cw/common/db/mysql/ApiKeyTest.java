package cw.common.db.mysql;

import cwp.db.mysql.MySqlAdapter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ApiKeyTest {
    @BeforeAll
    static void beforeAll() {
        HibernateUtil.setHibernateMapping();
    }

    @Test
    void test_ApiKey() throws Exception {
        for (ApiKey apiKey : MySqlAdapter.getINSTANCE().readAll(ApiKey.class)) {
            System.out.println(apiKey);
        }
    }
}
