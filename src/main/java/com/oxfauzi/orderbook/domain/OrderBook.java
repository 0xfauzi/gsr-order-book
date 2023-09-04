package com.oxfauzi.orderbook.domain;

import com.oxfauzi.orderbook.command.order.AddOrderCommand;
import com.oxfauzi.orderbook.OrderBookPrinter;
import com.oxfauzi.orderbook.command.order.RemoveOrderCommand;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class OrderBook {


    private final TreeMap<BigDecimal, Level> buyLevels;
    private final TreeMap<BigDecimal, Level> sellLevels;
    private final Map<Order, OrderBookEntry> orderEntries;
    private final OrderBookPrinter orderBookPrinter;
    private final AddOrderCommand addOrderCommand;
    private final RemoveOrderCommand removeOrderCommand;

    public OrderBook(TreeMap<BigDecimal, Level> buyLevels,
                     TreeMap<BigDecimal, Level> sellLevels,
                     Map<Order, OrderBookEntry> orderEntries,
                     AddOrderCommand addOrderCommand,
                     RemoveOrderCommand removeOrderCommand,
                     OrderBookPrinter orderBookPrinter) {
        this.buyLevels = buyLevels;
        this.sellLevels = sellLevels;
        this.orderEntries = orderEntries;
        this.addOrderCommand = addOrderCommand;
        this.removeOrderCommand = removeOrderCommand;
        this.orderBookPrinter = orderBookPrinter;
    }

    public Map<Order, OrderBookEntry> getOrderEntries() {
        return Collections.unmodifiableMap(orderEntries);
    }

    public void addOrder(Order order) {
        if (order.isCancelOrder()) {
            removeOrder(order);
        } else {
            addOrderCommand.execute(order);
        }
    }

    public void removeOrder(Order order) {
        if (orderEntries.containsKey(order)) {
            removeOrderCommand.execute(order);
        }
    }

    public NavigableMap<BigDecimal, Level> getBuyLevels() {
        return Collections.unmodifiableNavigableMap(buyLevels);
    }

    public NavigableMap<BigDecimal, Level> getSellLevels() {
        return Collections.unmodifiableNavigableMap(sellLevels);
    }

    public void printOrderBook() {
        if (buyLevels.size() >= 10 && sellLevels.size() >= 10) {
            orderBookPrinter.printToConsole(this);
        }
    }

}
