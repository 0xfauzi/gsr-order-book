package com.gsr.orderbook.command.level;

import com.gsr.orderbook.util.DebugLogger;
import com.gsr.orderbook.domain.Level;
import com.gsr.orderbook.domain.Order;
import com.gsr.orderbook.domain.OrderBookEntry;

import java.math.BigDecimal;
import java.util.Map;
import java.util.NavigableMap;

public class UpdateLevelCommand implements LevelCommand {

    public UpdateLevelCommand() {
    }

    @Override
    public boolean execute(NavigableMap<BigDecimal, Level> levels, Level level, Order order, Map<Order, OrderBookEntry> orderEntries) {
        try {
            Level existingLevel = levels.get(level.getPrice());
            OrderBookEntry orderBookEntry = new OrderBookEntry(order, existingLevel);
            if (existingLevel.getHead() == null) {
                existingLevel.setHead(orderBookEntry);
                existingLevel.setTail(orderBookEntry);
            } else {
                OrderBookEntry tailPointer = existingLevel.getTail();
                tailPointer.setNext(orderBookEntry);
                orderBookEntry.setPrevious(tailPointer);
                existingLevel.setTail(orderBookEntry);
            }
            return orderEntries.put(order, orderBookEntry) != null;
        } catch (Exception e) {
            DebugLogger.log(" | UPDATE_LEVEL | Exception: " + e.getMessage());
            return false;
        }
    }
}
