package cw.common.core;

import cw.common.db.mysql.Exchange;
import cw.common.db.mysql.HibernateUtil;
import cw.common.event.EventQueue;
import cw.common.timer.ITimeManager;
import cw.common.timer.TimeManager;
import cw.common.timer.TimerQueue;
import cwp.db.mysql.MySqlAdapter;

import java.util.Map;

public abstract class AbstractServer {
    protected final MySqlAdapter dbAdapter;
    protected final ITimeManager timeManager;
    protected EventQueue eventQueue;
    protected TimerQueue timerQueue;

    public AbstractServer() throws Exception {
        HibernateUtil.setHibernateMapping();

        this.timeManager = new TimeManager();
        this.dbAdapter = MySqlAdapter.getINSTANCE();
    }

    protected abstract Map<Exchange, ExchangeApiHandler> generateExchangeApiHandlers();
}
