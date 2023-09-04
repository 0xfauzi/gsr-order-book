package com.oxfauzi.orderbook.command.level;

import com.oxfauzi.orderbook.domain.Level;
import com.oxfauzi.orderbook.domain.Order;
import com.oxfauzi.orderbook.domain.OrderBookEntry;
import com.oxfauzi.orderbook.util.DebugLogger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import static com.oxfauzi.orderbook.domain.Constants.EMPTY_LEVEL;
import static org.assertj.core.api.Assertions.assertThat;

class AddLevelCommandTest {
    private AddLevelCommand addLevelCommand;

    @BeforeAll
    static void initLogger() {
        DebugLogger.initializeForTest();
    }


    @BeforeEach
    void setup() {
        addLevelCommand = new AddLevelCommand();
    }

    @Test
    void givenALevel_whenExecuteIsCalled_addOrderAtLevel() {

        NavigableMap<BigDecimal, Level> levels = new TreeMap<>();
        Level level = new Level(BigDecimal.valueOf(3.2));
        Order order = new Order("buy", BigDecimal.valueOf(3.2), BigDecimal.valueOf(0.2123));
        Map<Order, OrderBookEntry> orderEntries = new HashMap<>();

        OrderBookEntry orderBookEntry = new OrderBookEntry(order, level);

        addLevelCommand.execute(levels, level, order, orderEntries);

        assertThat(levels.keySet()).hasSize(1);
        assertThat(levels.get(level.getPrice())).isEqualTo(level);
        assertThat(level.getHead()).isEqualTo(orderBookEntry);
        assertThat(level.getTail()).isEqualTo(orderBookEntry);
        assertThat(orderEntries.keySet()).hasSize(1);
        assertThat(orderEntries.containsKey(order)).isTrue();

        assertThat(addLevelCommand.getMinBuyLevel()).isEqualTo(level);
        assertThat(addLevelCommand.getMaxSellLevel()).isEqualTo(EMPTY_LEVEL);
    }
}