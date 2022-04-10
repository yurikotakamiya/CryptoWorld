package cw.monitor.monitor.rsi;

import cw.common.db.mysql.*;
import cw.common.md.Candlestick;
import cw.common.timer.TimeManager;
import cw.common.timer.Timer;
import cw.common.timer.TimerType;
import cw.monitor.handler.binance.BinanceApiHandlerMock;
import net.openhft.chronicle.map.ChronicleMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class RsiMarketMonitorTest {
    private Candlestick candlestick;
    private BinanceApiHandlerMock apiHandler;
    private RsiMarketMonitor monitor;
    private MonitorConfig monitorConfig;

    @BeforeEach
    void before() {
        this.candlestick = Mockito.mock(Candlestick.class);
        this.apiHandler = new BinanceApiHandlerMock();
        this.monitor = new RsiMarketMonitor(this.candlestick, new HashMap<>(), null, new TimeManager(), t -> {
        }, Exchange.BINANCE, this.apiHandler, TradingPair.BTCUSDT);
        this.monitorConfig = getMonitorConfig();

        this.monitor.getChronicleMaps().put(CandlestickInterval.ONE_MINUTE, Mockito.mock(ChronicleMap.class));
    }

    private MonitorConfig getMonitorConfig() {
        MonitorConfig monitorConfig = new MonitorConfig();
        monitorConfig.setUserId(1);
        monitorConfig.setMonitor((byte) MonitorType.RSI.ordinal());
        monitorConfig.setExchange((byte) Exchange.BINANCE.ordinal());
        monitorConfig.setTradingPair((byte) TradingPair.BTCUSDT.ordinal());
        monitorConfig.setParamRsiTimeInterval((byte) CandlestickInterval.ONE_MINUTE.ordinal());
        monitorConfig.setParamRsiLowThreshold(25d);
        monitorConfig.setParamRsiHighThreshold(75d);
        return monitorConfig;
    }

    private com.binance.api.client.domain.market.Candlestick getCandlestick(long openTime, long closeTime, double openPrice, double closePrice) {
        com.binance.api.client.domain.market.Candlestick candlestick = new com.binance.api.client.domain.market.Candlestick();
        candlestick.setOpenTime(openTime);
        candlestick.setCloseTime(closeTime);
        candlestick.setOpen(String.valueOf(openPrice));
        candlestick.setClose(String.valueOf(closePrice));
        return candlestick;
    }

    private void invokeCandlestickTimer(long openTime, double openPrice, double closePrice) {
        Mockito.when(this.candlestick.getOpenTime()).thenReturn(openTime);
        Mockito.when(this.candlestick.getOpenPrice()).thenReturn(openPrice);
        Mockito.when(this.candlestick.getClosePrice()).thenReturn(closePrice);

        Timer timer = new Timer(1, TimerType.CANDLESTICK, 0, Exchange.BINANCE, TradingPair.BTCUSDT);
        this.monitor.onTimerEvent(timer);
    }

    @Test
    void test_Rsi_InitialCandlesticksEqualsLimit() {
        List<com.binance.api.client.domain.market.Candlestick> list = new LinkedList<>();
        list.add(getCandlestick(0, 60000 - 1, 44.34, 44.34)); // 0
        list.add(getCandlestick(60000, 60000 * 2 - 1, 44.34, 44.09)); // -0.25
        list.add(getCandlestick(60000 * 2, 60000 * 3 - 1, 44.09, 44.15)); // 0.06
        list.add(getCandlestick(60000 * 3, 60000 * 4 - 1, 44.15, 43.61)); // -0.54
        list.add(getCandlestick(60000 * 4, 60000 * 5 - 1, 43.61, 44.33)); // 0.72
        list.add(getCandlestick(60000 * 5, 60000 * 6 - 1, 44.33, 44.83)); // 0.50
        list.add(getCandlestick(60000 * 6, 60000 * 7 - 1, 44.83, 45.10)); // 0.27
        list.add(getCandlestick(60000 * 7, 60000 * 8 - 1, 45.10, 45.42)); // 0.32
        list.add(getCandlestick(60000 * 8, 60000 * 9 - 1, 45.42, 45.84)); // 0.42
        list.add(getCandlestick(60000 * 9, 60000 * 10 - 1, 45.84, 46.08)); // 0.24
        list.add(getCandlestick(60000 * 10, 60000 * 11 - 1, 46.08, 45.89)); // -0.19
        list.add(getCandlestick(60000 * 11, 60000 * 12 - 1, 45.89, 46.03)); // 0.14
        list.add(getCandlestick(60000 * 12, 60000 * 13 - 1, 46.03, 45.61)); // -0.42
        list.add(getCandlestick(60000 * 13, 60000 * 14 - 1, 45.61, 46.28)); // 0.67
        this.apiHandler.setHistoricalCandlesticks(list);

        this.monitor.onMonitorConfig(this.monitorConfig);
        Assertions.assertEquals(0.24, this.monitor.getAverageGains().get(CandlestickInterval.ONE_MINUTE), 0.01);
        Assertions.assertEquals(-0.1, this.monitor.getAverageLosses().get(CandlestickInterval.ONE_MINUTE), 0.01);

        invokeCandlestickTimer(60000 * 14, 46.28, 46);
        Assertions.assertEquals(0.24, this.monitor.getAverageGains().get(CandlestickInterval.ONE_MINUTE), 0.01);
        Assertions.assertEquals(-0.1, this.monitor.getAverageLosses().get(CandlestickInterval.ONE_MINUTE), 0.01);

        invokeCandlestickTimer(60000 * 15, 46, 46.03);
        Assertions.assertEquals(0.22, this.monitor.getAverageGains().get(CandlestickInterval.ONE_MINUTE), 0.01);
        Assertions.assertEquals(-0.11, this.monitor.getAverageLosses().get(CandlestickInterval.ONE_MINUTE), 0.01);

        invokeCandlestickTimer(60000 * 16, 46.03, 46.41);
        Assertions.assertEquals(0.21, this.monitor.getAverageGains().get(CandlestickInterval.ONE_MINUTE), 0.01);
        Assertions.assertEquals(-0.1, this.monitor.getAverageLosses().get(CandlestickInterval.ONE_MINUTE), 0.01);

        invokeCandlestickTimer(60000 * 17, 46.41, 46.22);
        Assertions.assertEquals(0.22, this.monitor.getAverageGains().get(CandlestickInterval.ONE_MINUTE), 0.01);
        Assertions.assertEquals(-0.1, this.monitor.getAverageLosses().get(CandlestickInterval.ONE_MINUTE), 0.01);

        invokeCandlestickTimer(60000 * 18, 46.22, 45.64);
        Assertions.assertEquals(0.20, this.monitor.getAverageGains().get(CandlestickInterval.ONE_MINUTE), 0.01);
        Assertions.assertEquals(-0.1, this.monitor.getAverageLosses().get(CandlestickInterval.ONE_MINUTE), 0.01);
    }

    @Test
    void test_Rsi_InitialCandlesticksGreaterThanLimit() {
        List<com.binance.api.client.domain.market.Candlestick> list = new LinkedList<>();
        list.add(getCandlestick(0, 60000 - 1, 44.34, 44.34)); // 0
        list.add(getCandlestick(60000, 60000 * 2 - 1, 44.34, 44.09)); // -0.25
        list.add(getCandlestick(60000 * 2, 60000 * 3 - 1, 44.09, 44.15)); // 0.06
        list.add(getCandlestick(60000 * 3, 60000 * 4 - 1, 44.15, 43.61)); // -0.54
        list.add(getCandlestick(60000 * 4, 60000 * 5 - 1, 43.61, 44.33)); // 0.72
        list.add(getCandlestick(60000 * 5, 60000 * 6 - 1, 44.33, 44.83)); // 0.50
        list.add(getCandlestick(60000 * 6, 60000 * 7 - 1, 44.83, 45.10)); // 0.27
        list.add(getCandlestick(60000 * 7, 60000 * 8 - 1, 45.10, 45.42)); // 0.32
        list.add(getCandlestick(60000 * 8, 60000 * 9 - 1, 45.42, 45.84)); // 0.42
        list.add(getCandlestick(60000 * 9, 60000 * 10 - 1, 45.84, 46.08)); // 0.24
        list.add(getCandlestick(60000 * 10, 60000 * 11 - 1, 46.08, 45.89)); // -0.19
        list.add(getCandlestick(60000 * 11, 60000 * 12 - 1, 45.89, 46.03)); // 0.14
        list.add(getCandlestick(60000 * 12, 60000 * 13 - 1, 46.03, 45.61)); // -0.42
        list.add(getCandlestick(60000 * 13, 60000 * 14 - 1, 45.61, 46.28)); // 0.67
        list.add(getCandlestick(60000 * 14, 60000 * 15 - 1, 46.28, 46)); // -0.28
        list.add(getCandlestick(60000 * 15, 60000 * 16 - 1, 46, 46.03)); // 0.03
        list.add(getCandlestick(60000 * 16, 60000 * 17 - 1, 46.03, 46.41)); // 0.38
        this.apiHandler.setHistoricalCandlesticks(list);

        this.monitor.onMonitorConfig(this.monitorConfig);
        Assertions.assertEquals(0.21, this.monitor.getAverageGains().get(CandlestickInterval.ONE_MINUTE), 0.01);
        Assertions.assertEquals(-0.1, this.monitor.getAverageLosses().get(CandlestickInterval.ONE_MINUTE), 0.01);

        invokeCandlestickTimer(60000 * 17, 46.41, 46.22);
        Assertions.assertEquals(0.22, this.monitor.getAverageGains().get(CandlestickInterval.ONE_MINUTE), 0.01);
        Assertions.assertEquals(-0.1, this.monitor.getAverageLosses().get(CandlestickInterval.ONE_MINUTE), 0.01);

        invokeCandlestickTimer(60000 * 18, 46.22, 45.64);
        Assertions.assertEquals(0.20, this.monitor.getAverageGains().get(CandlestickInterval.ONE_MINUTE), 0.01);
        Assertions.assertEquals(-0.1, this.monitor.getAverageLosses().get(CandlestickInterval.ONE_MINUTE), 0.01);
    }
}
