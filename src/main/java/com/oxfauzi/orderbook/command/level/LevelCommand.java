package com.oxfauzi.orderbook.command.level;

import com.oxfauzi.orderbook.domain.Level;
import com.oxfauzi.orderbook.domain.Order;
import com.oxfauzi.orderbook.domain.OrderBookEntry;

import java.math.BigDecimal;
import java.util.Map;
import java.util.NavigableMap;

public interface LevelCommand {
   boolean execute(NavigableMap<BigDecimal, Level> levels, Level level, Order order, Map<Order, OrderBookEntry> orderEntries);
}
