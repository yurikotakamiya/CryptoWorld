package cw.trader.handler.binance;

import com.binance.api.client.BinanceApiAsyncRestClient;
import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.domain.OrderStatus;
import com.binance.api.client.domain.account.NewOrderResponse;
import com.binance.api.client.domain.account.Trade;
import cw.common.db.mysql.ApiKey;
import cw.common.id.IdGenerator;
import cw.common.db.mysql.OrderSide;
import cw.trader.strategy.AbstractTraderStrategy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.decimal4j.util.DoubleRounder;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class BinanceApiHandlerSimulator extends BinanceApiHandler {
    private static final Logger LOGGER = LogManager.getLogger(BinanceApiHandlerSimulator.class.getSimpleName());

    private static final int PARTITION_NEW = 25;
    private static final int PARTITION_PARTIAL_FILLED = 50;
    private static final int PARTITION_FILLED = 75;
    private static final int PARTITION_CANCELED = 95;
    private static final int PARTITION_SUBMIT_REJECT = 100;

    private static final double[] RATIOS_1 = new double[]{1};
    private static final double[] RATIOS_2 = new double[]{0.3, 0.7};
    private static final double[] RATIOS_3 = new double[]{0.2, 0.6, 0.2};
    private static final double[] RATIOS_4 = new double[]{0.2, 0.25, 0.15, 0.4};
    private static final double[] RATIOS_5 = new double[]{0.2, 0.1, 0.2, 0.3, 0.2};

    private final Random random;

    public BinanceApiHandlerSimulator() {
        super();

        this.random = new Random();
    }

    @Override
    public void add(ApiKey apiKey) {
        BinanceApiClientFactory factory = BinanceApiClientFactory.newInstance(apiKey.getApiKey(), apiKey.getSecretKey());
        BinanceApiAsyncRestClient asyncRestClient = factory.newAsyncRestClient();
        this.asyncRestClients.put(apiKey.getUserId(), asyncRestClient);

        LOGGER.info("API key received for user {}.", apiKey.getUserId());
    }

    @Override
    public void submitLimitFok(AbstractTraderStrategy strategy, int userId, long orderId, String orderSize, String orderPrice, double orderPriceDouble, OrderSide orderSide) {
        List<NewOrderResponse> responses = generateNewOrderResponses(Double.parseDouble(orderSize), orderPrice);

        for (NewOrderResponse response : responses) {
            handleNewOrderResponse(response, strategy, userId, orderId, orderPriceDouble, orderSide);
        }
    }

    private List<NewOrderResponse> generateNewOrderResponses(double orderSize, String orderPrice) {
        List<NewOrderResponse> list = new LinkedList<>();
        OrderStatus status = generateOrderStatus();

        if (status == OrderStatus.NEW) {
            String clientOrderId = String.valueOf(IdGenerator.nextOrderTradeId());

            NewOrderResponse newOrderResponse = new NewOrderResponse();
            newOrderResponse.setClientOrderId(clientOrderId);
            newOrderResponse.setStatus(status);
            newOrderResponse.setTransactTime(System.currentTimeMillis());

            list.add(newOrderResponse);

            newOrderResponse = new NewOrderResponse();
            newOrderResponse.setClientOrderId(clientOrderId);
            newOrderResponse.setStatus(OrderStatus.FILLED);
            newOrderResponse.setTransactTime(System.currentTimeMillis());
            newOrderResponse.setFills(getFills(orderSize, orderPrice));

            list.add(newOrderResponse);
        } else if (status == OrderStatus.PARTIALLY_FILLED) {
            String clientOrderId = String.valueOf(IdGenerator.nextOrderTradeId());

            NewOrderResponse newOrderResponse = new NewOrderResponse();
            newOrderResponse.setClientOrderId(clientOrderId);
            newOrderResponse.setStatus(OrderStatus.NEW);
            newOrderResponse.setTransactTime(System.currentTimeMillis());

            list.add(newOrderResponse);

            newOrderResponse = new NewOrderResponse();
            newOrderResponse.setClientOrderId(clientOrderId);
            newOrderResponse.setStatus(status);
            newOrderResponse.setTransactTime(System.currentTimeMillis());
            newOrderResponse.setFills(getFills(orderSize * 2 / 3, orderPrice));

            list.add(newOrderResponse);

            newOrderResponse = new NewOrderResponse();
            newOrderResponse.setClientOrderId(clientOrderId);
            newOrderResponse.setStatus(OrderStatus.FILLED);
            newOrderResponse.setTransactTime(System.currentTimeMillis());
            newOrderResponse.setFills(getFills(orderSize / 3, orderPrice));

            list.add(newOrderResponse);
        } else if (status == OrderStatus.FILLED) {
            NewOrderResponse newOrderResponse = new NewOrderResponse();
            newOrderResponse.setClientOrderId(String.valueOf(IdGenerator.nextOrderTradeId()));
            newOrderResponse.setStatus(status);
            newOrderResponse.setTransactTime(System.currentTimeMillis());
            newOrderResponse.setFills(getFills(orderSize, orderPrice));

            list.add(newOrderResponse);
        } else if (status == OrderStatus.CANCELED || status == OrderStatus.REJECTED) {
            NewOrderResponse newOrderResponse = new NewOrderResponse();
            newOrderResponse.setClientOrderId(String.valueOf(IdGenerator.nextOrderTradeId()));
            newOrderResponse.setStatus(status);
            newOrderResponse.setTransactTime(System.currentTimeMillis());

            list.add(newOrderResponse);
        }

        return list;
    }

    private OrderStatus generateOrderStatus() {
        OrderStatus orderStatus = null;
        int value = this.random.nextInt(100);

        if (value < PARTITION_NEW) {
            orderStatus = OrderStatus.NEW;
        } else if (value < PARTITION_PARTIAL_FILLED) {
            orderStatus = OrderStatus.PARTIALLY_FILLED;
        } else if (value < PARTITION_FILLED) {
            orderStatus = OrderStatus.FILLED;
        } else if (value < PARTITION_CANCELED) {
            orderStatus = OrderStatus.CANCELED;
        } else if (value < PARTITION_SUBMIT_REJECT) {
            orderStatus = OrderStatus.REJECTED;
        }

        return orderStatus;
    }

    private List<Trade> getFills(double orderSize, String orderPrice) {
        List<Trade> list = new LinkedList<>();
        int count = this.random.nextInt(5) + 1;

        if (count == RATIOS_1.length) {
            for (int i = 0; i < RATIOS_1.length; i++) {
                Trade trade = new Trade();
                trade.setQty(String.valueOf(DoubleRounder.round(orderSize * RATIOS_1[i], 5)));
                trade.setPrice(orderPrice);

                list.add(trade);
            }
        } else if (count == RATIOS_2.length) {
            for (int i = 0; i < RATIOS_2.length; i++) {
                Trade trade = new Trade();
                trade.setQty(String.valueOf(DoubleRounder.round(orderSize * RATIOS_2[i], 5)));
                trade.setPrice(orderPrice);

                list.add(trade);
            }
        } else if (count == RATIOS_3.length) {
            for (int i = 0; i < RATIOS_3.length; i++) {
                Trade trade = new Trade();
                trade.setQty(String.valueOf(DoubleRounder.round(orderSize * RATIOS_3[i], 5)));
                trade.setPrice(orderPrice);

                list.add(trade);
            }
        } else if (count == RATIOS_4.length) {
            for (int i = 0; i < RATIOS_4.length; i++) {
                Trade trade = new Trade();
                trade.setQty(String.valueOf(DoubleRounder.round(orderSize * RATIOS_4[i], 5)));
                trade.setPrice(orderPrice);

                list.add(trade);
            }
        } else if (count == RATIOS_5.length) {
            for (int i = 0; i < RATIOS_5.length; i++) {
                Trade trade = new Trade();
                trade.setQty(String.valueOf(DoubleRounder.round(orderSize * RATIOS_5[i], 5)));
                trade.setPrice(orderPrice);

                list.add(trade);
            }
        }

        return list;
    }
}
