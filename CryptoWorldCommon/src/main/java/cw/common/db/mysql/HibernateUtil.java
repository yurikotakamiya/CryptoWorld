package cw.common.db.mysql;

import cwp.db.mysql.MySqlAdapter;

public class HibernateUtil {
    public static void setHibernateMapping() {
        MySqlAdapter.addAnnotatedClass(User.class);
        MySqlAdapter.addAnnotatedClass(Order.class);
        MySqlAdapter.addAnnotatedClass(Trade.class);
        MySqlAdapter.addAnnotatedClass(ApiKey.class);
        MySqlAdapter.addAnnotatedClass(StrategyConfig.class);
        MySqlAdapter.addAnnotatedClass(MonitorConfig.class);
    }
}
