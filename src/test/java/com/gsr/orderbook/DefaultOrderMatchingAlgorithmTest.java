package com.gsr.orderbook;

import com.gsr.orderbook.command.level.AddLevelCommand;
import com.gsr.orderbook.command.level.UpdateLevelCommand;
import com.gsr.orderbook.command.order.AddOrderCommand;
import com.gsr.orderbook.command.order.RemoveOrderCommand;
import com.gsr.orderbook.domain.Level;
import com.gsr.orderbook.domain.Order;
import com.gsr.orderbook.domain.OrderBook;
import com.gsr.orderbook.domain.OrderBookEntry;
import com.gsr.orderbook.matching.DefaultOrderMatchingAlgorithm;
import com.gsr.orderbook.util.BigDecimalUtils;
import com.gsr.orderbook.util.DebugLogger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static java.math.BigDecimal.*;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class DefaultOrderMatchingAlgorithmTest {

    private OrderBook orderBook;
    private TreeMap<BigDecimal, Level> buyLevels;
    private TreeMap<BigDecimal, Level> sellLevels;;
    private Map<Order, OrderBookEntry> orderEntries;

    private AddOrderCommand addOrderOperation;
    private RemoveOrderCommand removeOrderOperation;

    @Mock
    private OrderBookPrinter orderBookPrinter;
    private DefaultOrderMatchingAlgorithm defaultOrderMatchingAlgorithm;

    @BeforeAll
    static void initLogger() {
        DebugLogger.initializeForTest();
    }

    @BeforeEach
    void setUp() {
        buyLevels = new TreeMap<>(Comparator.reverseOrder());
        sellLevels = new TreeMap<>(Comparator.naturalOrder());
        orderEntries = new HashMap<>();

        removeOrderOperation = new RemoveOrderCommand(buyLevels, sellLevels, orderEntries);
        UpdateLevelCommand updateLevelCommand = new UpdateLevelCommand();
        AddLevelCommand addLevelCommand = new AddLevelCommand();
        addOrderOperation = new AddOrderCommand(updateLevelCommand, addLevelCommand, buyLevels, sellLevels, orderEntries);

        OrderBookPrinter orderBookPrinter = new OrderBookPrinter();


        orderBook = new OrderBook(buyLevels, sellLevels, orderEntries, addOrderOperation, removeOrderOperation, orderBookPrinter);
        defaultOrderMatchingAlgorithm = new DefaultOrderMatchingAlgorithm(orderBook, buyLevels, sellLevels);
    }

    @Test
    void givenNoSellOrders_whenBuyOrderIsPassed_buyOrderShouldBeAddedToOrderBook() {
        Order buyOrder = new Order("buy", valueOf(1.2), valueOf(5.3));

        List<Order> orders = defaultOrderMatchingAlgorithm.matchOrders(buyOrder);

        assertThat(orders).isEmpty();
        assertThat(orderBook.getOrderEntries()).containsKey(buyOrder);
        assertThat(orderBook.getBuyLevels()).containsKey(valueOf(1.2));
    }

    @Test
    void givenABuyOrder_whenThereIsASellOrderWithTheSamePriceAndQuantity_matchThatSellOrder() {
        Order buyOrder = new Order("buy", valueOf(1.2), valueOf(5.3));
        Order matchingSellOrder = new Order("sell", valueOf(1.2), valueOf(5.3));
        orderBook.addOrder(matchingSellOrder);

        List<Order> matchingOrders = defaultOrderMatchingAlgorithm.matchOrders(buyOrder);

        assertThat(matchingOrders).hasSize(1);
        assertThat(matchingOrders).contains(matchingSellOrder);
        assertThat(orderBook.getOrderEntries()).isEmpty();
        assertThat(orderBook.getBuyLevels()).isEmpty();
        assertThat(orderBook.getSellLevels()).isEmpty();
    }

    @Test
    void givenABuyOrder_whenThereAreNoLowerSellOrders_doNotMatch() {
        Order buyOrder = new Order("buy", valueOf(1.2), valueOf(5.3));
        Order matchingSellOrder = new Order("sell", valueOf(1.3), valueOf(5.3));
        orderBook.addOrder(matchingSellOrder);

        List<Order> matchingOrders = defaultOrderMatchingAlgorithm.matchOrders(buyOrder);

        assertThat(matchingOrders).isEmpty();
        assertThat(orderBook.getOrderEntries()).hasSize(2);
        assertThat(orderBook.getBuyLevels().keySet()).hasSize(1);
        assertThat(orderBook.getBuyLevels().keySet()).contains(buyOrder.getPrice());
        assertThat(orderBook.getSellLevels().keySet()).hasSize(1);
        assertThat(orderBook.getSellLevels().keySet()).contains(matchingSellOrder.getPrice());
    }

    @Test
    void givenABuyOrder_whenThereIsASellOrderWithALowerPriceAndSameQuantity_matchThatSellOrder() {
        Order buyOrder = new Order("buy", valueOf(1.2), valueOf(5.3));
        Order matchingSellOrder = new Order("sell", valueOf(1.1), valueOf(5.3));
        orderBook.addOrder(matchingSellOrder);

        List<Order> matchingOrders = defaultOrderMatchingAlgorithm.matchOrders(buyOrder);

        assertThat(matchingOrders).hasSize(1);
        assertThat(matchingOrders).contains(matchingSellOrder);
        assertThat(orderBook.getOrderEntries()).isEmpty();
        assertThat(orderBook.getBuyLevels()).isEmpty();
        assertThat(orderBook.getSellLevels()).isEmpty();
    }

    @Test
    void givenABuyOrder_whenThereAreMultipleSellOrdersWithALowerPriceAndSameQuantity_matchOnlyTheLowestPriceSellOrder() {
        Order buyOrder = new Order("buy", valueOf(1.2), valueOf(5.3));

        Order matchingSellOrder1 = new Order("sell", valueOf(1.1), valueOf(5.3));
        Order matchingSellOrder2 = new Order("sell", valueOf(1.01), valueOf(5.3));
        orderBook.addOrder(matchingSellOrder1);
        orderBook.addOrder(matchingSellOrder2);

        List<Order> matchingOrders = defaultOrderMatchingAlgorithm.matchOrders(buyOrder);

        assertThat(matchingOrders).hasSize(1);
        assertThat(matchingOrders).contains(matchingSellOrder2);

        assertThat(orderBook.getOrderEntries()).hasSize(1);
        assertThat(orderBook.getOrderEntries()).containsKey(matchingSellOrder1);

        assertThat(orderBook.getBuyLevels()).isEmpty();

        assertThat(orderBook.getSellLevels()).hasSize(1);
        assertThat(orderBook.getSellLevels()).containsKey(valueOf(1.1));
    }

    @Test
    void givenABuyOrderMatchingPrice_whenThereIsNoExactQuantityMatch_matchAllOrdersAtThatPriceLevelWithEnoughQuantity() {

        Order buyOrder = new Order("buy", valueOf(1.2), valueOf(11.713571970));

        Order matchingSellOrder1 = new Order("sell", valueOf(1.1), valueOf(5.3));
        orderBook.addOrder(matchingSellOrder1);

        Order matchingSellOrder2 = new Order("sell", valueOf(1.01), valueOf(5.3));
        orderBook.addOrder(matchingSellOrder2);

        Order matchingSellOrder3 = new Order("sell", valueOf(1.01), valueOf(5.41357197));
        orderBook.addOrder(matchingSellOrder3);

        Order matchingSellOrder4 = new Order("sell", valueOf(1.01), valueOf(1.0));
        orderBook.addOrder(matchingSellOrder4);

        List<Order> matchingOrders = defaultOrderMatchingAlgorithm.matchOrders(buyOrder);

        assertThat(matchingOrders).hasSize(3);
        assertThat(matchingOrders).contains(matchingSellOrder2, matchingSellOrder3, matchingSellOrder4);

        assertThat(orderBook.getOrderEntries()).hasSize(1);
        assertThat(orderBook.getOrderEntries()).containsKey(matchingSellOrder1);

        assertThat(orderBook.getBuyLevels()).isEmpty();

        assertThat(orderBook.getSellLevels()).hasSize(1);
        assertThat(orderBook.getSellLevels()).containsKey(valueOf(1.1));
    }

    @Test
    void givenABuyOrderMatchingPrice_whenThereIsNoExactQuantityMatchInOneLevel_matchAllOrdersInAllPriceLevelsWithEnoughQuantity() {

        Order buyOrder = new Order("buy", valueOf(1.2), valueOf(11.713571970));

        Order matchingSellOrder1 = new Order("sell", valueOf(1.1), valueOf(5.3));
        orderBook.addOrder(matchingSellOrder1);

        Order matchingSellOrder2 = new Order("sell", valueOf(1.01), valueOf(5.3));
        orderBook.addOrder(matchingSellOrder2);

        Order matchingSellOrder3 = new Order("sell", valueOf(1.002), valueOf(5.41357197));
        orderBook.addOrder(matchingSellOrder3);

        Order matchingSellOrder4 = new Order("sell", valueOf(1.002), valueOf(1.0));
        orderBook.addOrder(matchingSellOrder4);

        List<Order> matchingOrders = defaultOrderMatchingAlgorithm.matchOrders(buyOrder);

        assertThat(matchingOrders).hasSize(3);
        assertThat(matchingOrders).contains(matchingSellOrder2, matchingSellOrder3, matchingSellOrder4);

        assertThat(orderBook.getOrderEntries()).hasSize(1);
        assertThat(orderBook.getOrderEntries()).containsKey(matchingSellOrder1);

        assertThat(orderBook.getBuyLevels()).isEmpty();

        assertThat(orderBook.getSellLevels()).hasSize(1);
        assertThat(orderBook.getSellLevels()).containsKey(valueOf(1.1));
    }

    @Test
    void givenABuyOrderMatchingPrice_whenThereIsNoExactQuantityMatch_addLeftOverQuantityAsNewBuyOrder() {

        Order buyOrder = new Order("buy", valueOf(1.2), valueOf(13.813571970));

        Order matchingSellOrder1 = new Order("sell", valueOf(1.1), valueOf(5.3));
        orderBook.addOrder(matchingSellOrder1);

        Order matchingSellOrder2 = new Order("sell", valueOf(1.01), valueOf(5.3));
        orderBook.addOrder(matchingSellOrder2);

        Order matchingSellOrder3 = new Order("sell", valueOf(1.002), valueOf(5.41357197));
        orderBook.addOrder(matchingSellOrder3);

        Order matchingSellOrder4 = new Order("sell", valueOf(1.002), valueOf(1.0));
        orderBook.addOrder(matchingSellOrder4);

        Order addedMatchingOrder = new Order("buy", valueOf(1.2), valueOf(2.1));

        List<Order> matchingOrders = defaultOrderMatchingAlgorithm.matchOrders(buyOrder);

        assertThat(matchingOrders).hasSize(3);
        assertThat(matchingOrders).contains(matchingSellOrder2, matchingSellOrder3, matchingSellOrder4);

        assertThat(orderBook.getOrderEntries()).hasSize(2);
        assertThat(orderBook.getOrderEntries().keySet()).extracting("quantity").anyMatch(q -> BigDecimalUtils.equal(matchingSellOrder2.getQuantity(), (BigDecimal) q));
        assertThat(orderBook.getOrderEntries().keySet()).extracting("quantity").anyMatch(q -> BigDecimalUtils.equal(addedMatchingOrder.getQuantity(), (BigDecimal) q));

        assertThat(orderBook.getBuyLevels()).hasSize(1);
        assertThat(orderBook.getBuyLevels().keySet()).contains(addedMatchingOrder.getPrice());

        assertThat(orderBook.getSellLevels()).hasSize(1);
        assertThat(orderBook.getSellLevels()).containsKey(valueOf(1.1));
    }

    @Test
    void givenNoBuyOrders_whenSellOrderIsPassed_sellOrderShouldBeAddedToOrderBook() {
        Order sellOrder = new Order("sell", valueOf(1.2), valueOf(5.3));

        List<Order> orders = defaultOrderMatchingAlgorithm.matchOrders(sellOrder);

        assertThat(orders).isEmpty();
        assertThat(orderBook.getOrderEntries()).containsKey(sellOrder);
        assertThat(orderBook.getSellLevels()).containsKey(valueOf(1.2));
    }

    @Test
    void givenASellOrder_whenThereIsABuyOrderWithTheSamePriceAndQuantity_matchThatBuyOrder() {
        Order sellOrder = new Order("sell", valueOf(1.2), valueOf(5.3));
        Order matchingBuyOrder = new Order("buy", valueOf(1.2), valueOf(5.3));
        orderBook.addOrder(matchingBuyOrder);

        List<Order> matchingOrders = defaultOrderMatchingAlgorithm.matchOrders(sellOrder);

        assertThat(matchingOrders).hasSize(1);
        assertThat(matchingOrders).contains(matchingBuyOrder);
        assertThat(orderBook.getOrderEntries()).isEmpty();
        assertThat(orderBook.getBuyLevels()).isEmpty();
        assertThat(orderBook.getSellLevels()).isEmpty();
    }

    @Test
    void givenASellOrder_whenThereAreNoHigherBuyOrders_doNotMatch() {
        Order sellOrder = new Order("sell", valueOf(1.2), valueOf(5.3));
        Order matchingBuyOrder = new Order("buy", valueOf(1.1), valueOf(5.3));
        orderBook.addOrder(matchingBuyOrder);

        List<Order> matchingOrders = defaultOrderMatchingAlgorithm.matchOrders(sellOrder);

        assertThat(matchingOrders).isEmpty();
        assertThat(orderBook.getOrderEntries()).hasSize(2);
        assertThat(orderBook.getBuyLevels().keySet()).hasSize(1);
        assertThat(orderBook.getBuyLevels().keySet()).contains(matchingBuyOrder.getPrice());
        assertThat(orderBook.getSellLevels().keySet()).hasSize(1);
        assertThat(orderBook.getSellLevels().keySet()).contains(sellOrder.getPrice());
    }

    @Test
    void givenASellOrder_whenThereIsABuyOrderWithAHigherPriceAndSameQuantity_matchThatBuyOrder() {
        Order sellOrder = new Order("sell", valueOf(1.1), valueOf(5.3));
        Order matchingBuyOrder = new Order("buy", valueOf(1.2), valueOf(5.3));
        orderBook.addOrder(matchingBuyOrder);

        List<Order> matchingOrders = defaultOrderMatchingAlgorithm.matchOrders(sellOrder);

        assertThat(matchingOrders).hasSize(1);
        assertThat(matchingOrders).contains(matchingBuyOrder);
        assertThat(orderBook.getOrderEntries()).isEmpty();
        assertThat(orderBook.getBuyLevels()).isEmpty();
        assertThat(orderBook.getSellLevels()).isEmpty();
    }

    @Test
    void givenASellOrder_whenThereAreMultipleBuyOrdersWithAHigherPriceAndSameQuantity_matchOnlyTheHighestPriceBuyOrder() {
        Order sellOrder = new Order("sell", valueOf(1.2), valueOf(5.3));

        Order matchingBuyOrder1 = new Order("buy", valueOf(1.3), valueOf(5.3));
        Order matchingBuyOrder2 = new Order("buy", valueOf(1.31), valueOf(5.3));
        orderBook.addOrder(matchingBuyOrder1);
        orderBook.addOrder(matchingBuyOrder2);

        List<Order> matchingOrders = defaultOrderMatchingAlgorithm.matchOrders(sellOrder);

        assertThat(matchingOrders).hasSize(1);
        assertThat(matchingOrders).contains(matchingBuyOrder2);

        assertThat(orderBook.getOrderEntries()).hasSize(1);
        assertThat(orderBook.getOrderEntries()).containsKey(matchingBuyOrder1);

        assertThat(orderBook.getSellLevels()).isEmpty();

        assertThat(orderBook.getBuyLevels()).hasSize(1);
        assertThat(orderBook.getBuyLevels()).containsKey(valueOf(1.3));
    }

    @Test
    void givenASellOrderMatchingPrice_whenThereIsNoExactQuantityMatch_matchAllOrdersAtThatPriceLevelWithEnoughQuantity() {

        Order sellOrder = new Order("sell", valueOf(1.2), valueOf(11.713571970));

        Order matchingBuyOrder1 = new Order("buy", valueOf(1.1), valueOf(5.3));
        orderBook.addOrder(matchingBuyOrder1);

        Order matchingBuyOrder2 = new Order("buy", valueOf(1.3), valueOf(5.3));
        orderBook.addOrder(matchingBuyOrder2);

        Order matchingBuyOrder3 = new Order("buy", valueOf(1.31), valueOf(5.41357197));
        orderBook.addOrder(matchingBuyOrder3);

        Order matchingBuyOrder4 = new Order("buy", valueOf(1.31), valueOf(1.0));
        orderBook.addOrder(matchingBuyOrder4);

        List<Order> matchingOrders = defaultOrderMatchingAlgorithm.matchOrders(sellOrder);

        assertThat(matchingOrders).hasSize(3);
        assertThat(matchingOrders).contains(matchingBuyOrder2, matchingBuyOrder3, matchingBuyOrder4);

        assertThat(orderBook.getOrderEntries()).hasSize(1);
        assertThat(orderBook.getOrderEntries()).containsKey(matchingBuyOrder1);

        assertThat(orderBook.getSellLevels()).isEmpty();

        assertThat(orderBook.getBuyLevels()).hasSize(1);
        assertThat(orderBook.getBuyLevels()).containsKey(valueOf(1.1));
    }

    @Test
    void givenASellOrderMatchingPrice_whenThereIsNoExactQuantityMatchInOneLevel_matchAllOrdersInAllPriceLevelsWithEnoughQuantity() {

        Order sellOrder = new Order("sell", valueOf(1.2), valueOf(11.713571970));

        Order matchingBuyOrder1 = new Order("buy", valueOf(1.1), valueOf(5.3));
        orderBook.addOrder(matchingBuyOrder1);

        Order matchingBuyOrder2 = new Order("buy", valueOf(1.3), valueOf(5.3));
        orderBook.addOrder(matchingBuyOrder2);

        Order matchingBuyOrder3 = new Order("buy", valueOf(1.31), valueOf(5.41357197));
        orderBook.addOrder(matchingBuyOrder3);

        Order matchingBuyOrder4 = new Order("buy", valueOf(1.31), valueOf(1.0));
        orderBook.addOrder(matchingBuyOrder4);

        List<Order> matchingOrders = defaultOrderMatchingAlgorithm.matchOrders(sellOrder);

        assertThat(matchingOrders).hasSize(3);
        assertThat(matchingOrders).contains(matchingBuyOrder2, matchingBuyOrder3, matchingBuyOrder4);

        assertThat(orderBook.getOrderEntries()).hasSize(1);
        assertThat(orderBook.getOrderEntries()).containsKey(matchingBuyOrder1);

        assertThat(orderBook.getSellLevels()).isEmpty();

        assertThat(orderBook.getBuyLevels()).hasSize(1);
        assertThat(orderBook.getBuyLevels()).containsKey(valueOf(1.1));
    }

    @Test
    void givenASellOrderMatchingPrice_whenThereIsNoExactQuantityMatch_addLeftOverQuantityAsNewSellOrder() {

        Order sellOrder = new Order("sell", valueOf(1.2), valueOf(13.813571970));

        Order matchingBuyOrder1 = new Order("buy", valueOf(1.1), valueOf(5.3));
        orderBook.addOrder(matchingBuyOrder1);

        Order matchingBuyOrder2 = new Order("buy", valueOf(1.3), valueOf(5.3));
        orderBook.addOrder(matchingBuyOrder2);

        Order matchingBuyOrder3 = new Order("buy", valueOf(1.31), valueOf(5.41357197));
        orderBook.addOrder(matchingBuyOrder3);

        Order matchingBuyOrder4 = new Order("buy", valueOf(1.31), valueOf(1.0));
        orderBook.addOrder(matchingBuyOrder4);

        Order addedMatchingOrder = new Order("sell", valueOf(1.2), valueOf(2.1));

        List<Order> matchingOrders = defaultOrderMatchingAlgorithm.matchOrders(sellOrder);

        assertThat(matchingOrders).hasSize(3);
        assertThat(matchingOrders).contains(matchingBuyOrder2, matchingBuyOrder3, matchingBuyOrder4);

        assertThat(orderBook.getOrderEntries()).hasSize(2);
        assertThat(orderBook.getOrderEntries().keySet()).extracting("quantity").anyMatch(q -> BigDecimalUtils.equal(matchingBuyOrder2.getQuantity(), (BigDecimal) q));
        assertThat(orderBook.getOrderEntries().keySet()).extracting("quantity").anyMatch(q -> BigDecimalUtils.equal(addedMatchingOrder.getQuantity(), (BigDecimal) q));

        assertThat(orderBook.getSellLevels()).hasSize(1);
        assertThat(orderBook.getSellLevels().keySet()).contains(addedMatchingOrder.getPrice());

        assertThat(orderBook.getBuyLevels()).hasSize(1);
        assertThat(orderBook.getBuyLevels()).containsKey(valueOf(1.1));
    }
}