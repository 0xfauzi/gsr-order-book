package com.gsr.orderbook.command.order;

import com.gsr.orderbook.util.DebugLogger;
import com.gsr.orderbook.command.level.AddLevelCommand;
import com.gsr.orderbook.command.level.UpdateLevelCommand;
import com.gsr.orderbook.domain.Level;
import com.gsr.orderbook.domain.Order;
import com.gsr.orderbook.domain.OrderBookEntry;

import java.math.BigDecimal;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class AddOrderCommand implements OrderBookCommand {

    private final UpdateLevelCommand updateLevelCommand;
    private final AddLevelCommand addLevelCommand;
    private final TreeMap<BigDecimal, Level> buyLevels;
    private final TreeMap<BigDecimal, Level> sellLevels;
    private final Map<Order, OrderBookEntry> orderEntries;


    public AddOrderCommand(UpdateLevelCommand updateLevelCommand,
                           AddLevelCommand addLevelCommand,
                           TreeMap<BigDecimal, Level> buyLevels,
                           TreeMap<BigDecimal, Level> sellLevels,
                           Map<Order, OrderBookEntry> orderEntries) {
        this.updateLevelCommand = updateLevelCommand;
        this.addLevelCommand = addLevelCommand;
        this.buyLevels = buyLevels;
        this.sellLevels = sellLevels;
        this.orderEntries = orderEntries;
    }

    @Override
    public void execute(Order order) {
        try {
            final Level newLevel = new Level(order.getPrice());
            final boolean isBuy = order.isBuy();
            final NavigableMap<BigDecimal, Level> levels = isBuy ? buyLevels : sellLevels;

            if (levels.containsKey(newLevel.getPrice())) {
                boolean successfullyUpdated = updateLevelCommand.execute(levels, newLevel, order, orderEntries);
                if (successfullyUpdated) {
                    DebugLogger.log(" | UPDATE_ORDER | Order book updated with a new order at an existing level: " + order);
                }
            } else {
                if (isBuy) {
                    boolean successfullyAddedBuyOrder = addLevelCommand.execute(buyLevels, newLevel, order, orderEntries);
                    if (successfullyAddedBuyOrder) {
                        DebugLogger.log(" | ADD_LEVEL, ADD_ORDER | New buy order added at new level: " + newLevel);
                    }
                } else {
                    boolean successfullyAddedSellOrder = addLevelCommand.execute(sellLevels, newLevel, order, orderEntries);
                    if (successfullyAddedSellOrder) {
                        DebugLogger.log(" | ADD_LEVEL, ADD_ORDER | New sell order added at new level: " + newLevel);
                    }
                }
            }
        } catch (Exception e) {
            DebugLogger.log(" | ADD_LEVEL, ADD_ORDER | Exception: " + e.getMessage());
        }

    }
}
