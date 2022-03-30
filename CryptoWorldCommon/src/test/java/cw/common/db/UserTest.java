package cw.common.db;

import cw.common.db.mysql.HibernateUtil;
import cw.common.db.mysql.User;
import cwp.db.mysql.MySqlAdapter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class UserTest {
    @BeforeAll
    static void beforeAll() {
        HibernateUtil.setHibernateMapping();
    }

    @Test
    void test_User() throws Exception {
        for (User user : MySqlAdapter.getINSTANCE().readAll(User.class)) {
            System.out.println(user);
        }
    }
}
