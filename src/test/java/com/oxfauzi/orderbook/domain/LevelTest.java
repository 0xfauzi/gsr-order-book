package com.oxfauzi.orderbook.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static com.oxfauzi.orderbook.domain.OrderType.BUY;
import static com.oxfauzi.orderbook.domain.OrderType.UNKNOWN;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class LevelTest {

    @Test
    void givenANullHeadAndTail_isEmpty_shouldReturnTrue() {
        Level level = new Level(BigDecimal.valueOf(5.23));
        assertThat(level.isEmpty()).isTrue();
    }

    @Test
    void givenANonNullHead_isEmpty_shouldReturnFalse() {
        Level level = new Level(BigDecimal.valueOf(5.23));
        level.setHead(new OrderBookEntry(new Order("buy", BigDecimal.ZERO, BigDecimal.ZERO), level));
        assertThat(level.isEmpty()).isFalse();
    }

    @Test
    void givenAnEmptyLevel_whenGetTypeIsCalled_unknownShouldBeReturned() {
        Level level = new Level(BigDecimal.valueOf(5.23));
        assertThat(level.getType()).isEqualTo(UNKNOWN);
    }

    @Test
    void givenALevel_whenGetTypeIsCalled_correctTypeIsReturned() {
        Level level = new Level(BigDecimal.valueOf(5.23));
        level.setHead(new OrderBookEntry(new Order("buy", BigDecimal.ZERO, BigDecimal.ZERO), level));
        assertThat(level.getType()).isEqualTo(BUY);
    }

    @Test
    void givenAnEmptyLevel_levelOrderCount_shouldBeZero() {
        Level level = new Level(BigDecimal.valueOf(5.23));
        assertThat(level.getLevelOrderCount()).isZero();
    }

    @Test
    void givenALevel_levelOrderCount_shouldReturnAccurateCountForNonZeroOrders() {
        Level level = new Level(BigDecimal.valueOf(1.2));
        OrderBookEntry orderBookEntry1 = new OrderBookEntry(new Order("buy", BigDecimal.valueOf(1.2), BigDecimal.valueOf(1.2)), level);
        OrderBookEntry orderBookEntry2 = new OrderBookEntry(new Order("buy", BigDecimal.valueOf(1.2), BigDecimal.valueOf(1.23)), level);
        OrderBookEntry orderBookEntry3 = new OrderBookEntry(new Order("buy", BigDecimal.valueOf(1.2), BigDecimal.ZERO), level);
        orderBookEntry1.setNext(orderBookEntry2);
        orderBookEntry2.setNext(orderBookEntry3);
        orderBookEntry2.setPrevious(orderBookEntry1);
        orderBookEntry3.setPrevious(orderBookEntry2);

        level.setHead(orderBookEntry1);
        level.setTail(orderBookEntry3);

        assertThat(level.getLevelOrderCount()).isEqualTo(2);
    }

    @Test
    void givenALevel_levelOrderQuantity_shouldReturnAccurateCountForNonZeroOrders() {

        Level level = new Level(BigDecimal.valueOf(1.2));
        OrderBookEntry orderBookEntry1 = new OrderBookEntry(new Order("buy", BigDecimal.valueOf(1.2), BigDecimal.valueOf(1.2)), level);
        OrderBookEntry orderBookEntry2 = new OrderBookEntry(new Order("buy", BigDecimal.valueOf(1.2), BigDecimal.valueOf(1.2)), level);
        OrderBookEntry orderBookEntry3 = new OrderBookEntry(new Order("buy", BigDecimal.valueOf(1.2), BigDecimal.ZERO), level);
        orderBookEntry1.setNext(orderBookEntry2);
        orderBookEntry2.setNext(orderBookEntry3);
        orderBookEntry2.setPrevious(orderBookEntry1);
        orderBookEntry3.setPrevious(orderBookEntry2);

        level.setHead(orderBookEntry1);
        level.setTail(orderBookEntry3);

        assertThat(level.getLevelOrderQuantity()).isEqualTo(BigDecimal.valueOf(2.4));

    }

    @Test
    void givenOrdersAtALevel_getOrders_shouldReturnAllOrdersAtThatLevel() {

        Level level = new Level(BigDecimal.valueOf(5.23));
        Order order1 = new Order("buy", BigDecimal.valueOf(1.2), BigDecimal.valueOf(1.2));
        Order order2 = new Order("buy", BigDecimal.valueOf(1.2), BigDecimal.valueOf(1.232));
        Order order3 = new Order("buy", BigDecimal.ZERO, BigDecimal.ZERO);

        OrderBookEntry orderBookEntry1 = new OrderBookEntry(order1, level);
        OrderBookEntry orderBookEntry2 = new OrderBookEntry(order2, level);
        OrderBookEntry orderBookEntry3 = new OrderBookEntry(order3, level);

        orderBookEntry1.setNext(orderBookEntry2);
        orderBookEntry2.setNext(orderBookEntry3);
        orderBookEntry2.setPrevious(orderBookEntry1);
        orderBookEntry3.setPrevious(orderBookEntry2);

        level.setHead(orderBookEntry1);
        level.setTail(orderBookEntry3);

        List<Order> orders = level.getOrders();

        Assertions.assertThat(orders).hasSize(2);
        Assertions.assertThat(orders).containsExactly(order1, order2);
    }
}