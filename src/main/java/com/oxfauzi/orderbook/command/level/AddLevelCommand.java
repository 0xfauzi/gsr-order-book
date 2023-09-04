package com.oxfauzi.orderbook.command.level;

import com.oxfauzi.orderbook.util.DebugLogger;
import com.oxfauzi.orderbook.domain.Level;
import com.oxfauzi.orderbook.domain.Order;
import com.oxfauzi.orderbook.domain.OrderBookEntry;
import com.oxfauzi.orderbook.domain.Constants;
import com.oxfauzi.orderbook.domain.OrderType;

import java.math.BigDecimal;
import java.util.Map;
import java.util.NavigableMap;

public class AddLevelCommand implements LevelCommand {

    private Level minBuyLevel;
    private Level maxSellLevel;

    public AddLevelCommand() {
        this.minBuyLevel = Constants.EMPTY_LEVEL;
        this.maxSellLevel = Constants.EMPTY_LEVEL;
    }

    @Override
    public boolean execute(NavigableMap<BigDecimal, Level> levels, Level level, Order order, Map<Order, OrderBookEntry> orderEntries) {
        try {
            levels.put(level.getPrice(), level);
            OrderBookEntry orderBookEntry = new OrderBookEntry(order, level);
            level.setHead(orderBookEntry);
            level.setTail(orderBookEntry);
            orderEntries.put(order, orderBookEntry);
            if (OrderType.BUY.equals(level.getType())) {
                minBuyLevel = new Level(levels.lastKey());
            } else {
                maxSellLevel = new Level(levels.lastKey());
            }
            DebugLogger.log(" | ADD_LEVEL | New level added: " + level);
            return true;
        } catch (Exception e) {
            DebugLogger.log(" | ADD_LEVEL | Exception: " + e.getMessage());
            return false;
        }
    }

    public Level getMinBuyLevel() {
        return minBuyLevel;
    }

    public Level getMaxSellLevel() {
        return maxSellLevel;
    }
}
