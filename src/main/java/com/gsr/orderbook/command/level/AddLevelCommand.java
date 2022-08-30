package com.gsr.orderbook.command.level;

import com.gsr.orderbook.util.DebugLogger;
import com.gsr.orderbook.domain.Level;
import com.gsr.orderbook.domain.Order;
import com.gsr.orderbook.domain.OrderBookEntry;

import java.math.BigDecimal;
import java.util.Map;
import java.util.NavigableMap;

import static com.gsr.orderbook.domain.Constants.EMPTY_LEVEL;
import static com.gsr.orderbook.domain.OrderType.BUY;

public class AddLevelCommand implements LevelCommand {

    private Level minBuyLevel;
    private Level maxSellLevel;

    public AddLevelCommand() {
        this.minBuyLevel = EMPTY_LEVEL;
        this.maxSellLevel = EMPTY_LEVEL;
    }

    @Override
    public boolean execute(NavigableMap<BigDecimal, Level> levels, Level level, Order order, Map<Order, OrderBookEntry> orderEntries) {
        try {
            levels.put(level.getPrice(), level);
            OrderBookEntry orderBookEntry = new OrderBookEntry(order, level);
            level.setHead(orderBookEntry);
            level.setTail(orderBookEntry);
            orderEntries.put(order, orderBookEntry);
            if (BUY.equals(level.getType())) {
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
