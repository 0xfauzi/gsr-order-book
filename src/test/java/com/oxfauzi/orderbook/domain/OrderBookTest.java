package com.oxfauzi.orderbook.domain;

import com.oxfauzi.orderbook.OrderBookPrinter;
import com.oxfauzi.orderbook.command.order.AddOrderCommand;
import com.oxfauzi.orderbook.command.order.RemoveOrderCommand;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Map;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderBookTest {

    @Mock
    private TreeMap<BigDecimal, Level> buyLevels;

    @Mock
    private TreeMap<BigDecimal, Level> sellLevels;

    @Mock
    private Map<Order, OrderBookEntry> orderEntries;

    @Mock
    private OrderBookPrinter orderBookPrinter;

    @Mock
    private AddOrderCommand addOrderCommand;

    @Mock
    private RemoveOrderCommand removeOrderCommand;

    private OrderBook orderBook;

    @BeforeEach
    void setUp() {
        orderBook = new OrderBook(buyLevels, sellLevels, orderEntries, addOrderCommand, removeOrderCommand, orderBookPrinter);
    }

    @Test
    void whenOrderIsACancelOrder_DoNotAdd_OnlyRemove() {

        final Order cancelOrder = new Order("buy", BigDecimal.valueOf(1.2), BigDecimal.ZERO);
        when(orderEntries.containsKey(cancelOrder)).thenReturn(true);

        orderBook.addOrder(cancelOrder);

        ArgumentCaptor<Order> argumentCaptor = ArgumentCaptor.forClass(Order.class);
        verifyNoInteractions(addOrderCommand);
        verify(removeOrderCommand, times(1)).execute(argumentCaptor.capture());
        Assertions.assertThat(argumentCaptor.getAllValues()).containsExactly(cancelOrder);
    }

    @Test
    void whenOrderIsNotCancelOrder_addToOrderBook() {

        final Order order = new Order("buy", BigDecimal.valueOf(1.2), BigDecimal.valueOf(2.3));

        orderBook.addOrder(order);

        ArgumentCaptor<Order> argumentCaptor = ArgumentCaptor.forClass(Order.class);
        verifyNoInteractions(removeOrderCommand);
        verify(addOrderCommand, times(1)).execute(argumentCaptor.capture());
        Assertions.assertThat(argumentCaptor.getAllValues()).containsExactly(order);
    }

    @Test
    void givenOrderEntryPresent_whenRemoveCalled_removeOrder() {

        final Order order = new Order("buy", BigDecimal.valueOf(1.2), BigDecimal.valueOf(2.3));
        when(orderEntries.containsKey(order)).thenReturn(true);

        orderBook.removeOrder(order);

        ArgumentCaptor<Order> argumentCaptor = ArgumentCaptor.forClass(Order.class);
        verifyNoInteractions(addOrderCommand);
        verify(removeOrderCommand, times(1)).execute(argumentCaptor.capture());
        Assertions.assertThat(argumentCaptor.getAllValues()).containsExactly(order);

    }

    @Test
    void givenOrderEntryNotPresent_whenRemoveCalled_doNothing() {

        final Order order = new Order("buy", BigDecimal.valueOf(1.2), BigDecimal.valueOf(2.3));
        when(orderEntries.containsKey(order)).thenReturn(false);

        orderBook.removeOrder(order);

        verifyNoInteractions(addOrderCommand);
        verifyNoInteractions(removeOrderCommand);
    }

    @Test
    void givenEnoughBuyAndSellLevels_whenPrintIsCalled_printOrderBook() {

        when(buyLevels.size()).thenReturn(10);
        when(sellLevels.size()).thenReturn(10);

        orderBook.printOrderBook();

        verify(orderBookPrinter, times(1)).printToConsole(orderBook);

    }

    @Test
    void givenLessThan10BuyOrSellLevels_whenPrintIsCalled_doNothing() {

        when(buyLevels.size()).thenReturn(10);
        when(sellLevels.size()).thenReturn(9);

        orderBook.printOrderBook();

        verifyNoInteractions(orderBookPrinter);

    }
}