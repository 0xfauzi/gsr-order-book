package com.gsr.orderbook.matching;

import com.gsr.orderbook.util.DebugLogger;
import com.gsr.orderbook.domain.Level;
import com.gsr.orderbook.domain.Order;
import com.gsr.orderbook.domain.OrderBook;

import java.math.BigDecimal;
import java.util.*;

import static com.gsr.orderbook.util.BigDecimalUtils.*;

public class DefaultOrderMatchingAlgorithm implements MatchingAlgorithm {

    private final OrderBook orderBook;
    private final NavigableMap<BigDecimal, Level> buyLevels;
    private final NavigableMap<BigDecimal, Level> sellLevels;

    public DefaultOrderMatchingAlgorithm(OrderBook orderBook,
                                         NavigableMap<BigDecimal, Level> buyLevels,
                                         NavigableMap<BigDecimal, Level> sellLevels) {
        this.orderBook = orderBook;
        this.buyLevels = buyLevels;
        this.sellLevels = sellLevels;
    }

    @Override
    public List<Order> matchOrders(Order order) {
        List<Order> matchingOrders = new ArrayList<>();
        Set<Order> ordersToRemove = new HashSet<>();

        if (order.isCancelOrder()) {
            DebugLogger.log(" | REMOVE_ORDER | Cancel order received, removing from order book: " + order);
            orderBook.removeOrder(order);
            return Collections.emptyList();
        }

        if (order.isBuy()) {
            if (sellLevels.isEmpty()) {
                orderBook.addOrder(order);
                return Collections.emptyList();
            }
            matchingOrders = processOrder(order, true, sellLevels, ordersToRemove);
        } else {
            if (buyLevels.isEmpty()) {
                orderBook.addOrder(order);
                return Collections.emptyList();
            }
            matchingOrders = processOrder(order, false, buyLevels, ordersToRemove);
        }

        removeOrders(ordersToRemove);

        return matchingOrders;
    }


    private List<Order> processOrder(Order order, boolean isBuy, NavigableMap<BigDecimal, Level> levels, Set<Order> ordersToRemove) {

        List<Order> matchingOrders = new ArrayList<>();

        BigDecimal orderPrice = order.getPrice();
        BigDecimal orderQuantity = order.getQuantity();
        for (BigDecimal nextLevelPrice : levels.navigableKeySet()) {

            if (equal(orderQuantity, BigDecimal.ZERO)) break;

            Level nextLevel = levels.get(nextLevelPrice);

            boolean pricesMatch = isBuy ? greaterThan(orderPrice, nextLevelPrice) || equal(orderPrice, nextLevelPrice) :
                    lessThan(orderPrice, nextLevelPrice) || equal(orderPrice, nextLevelPrice);
            if (pricesMatch) {
                for (Order nextLevelOrders : nextLevel.getOrders()) {
                    boolean quantitiesAreEqual = equal(orderQuantity, nextLevelOrders.getQuantity());
                    if (quantitiesAreEqual) {
                        ordersToRemove.add(nextLevelOrders);
                        DebugLogger.log(" | MATCH | Full price and quantity match found between " + order + " and " + nextLevelOrders);
                        matchingOrders.add(nextLevelOrders);
                        orderQuantity = BigDecimal.ZERO;
                        break;
                    } else {
                        if (greaterThan(orderQuantity, BigDecimal.ZERO) && greaterThan(orderQuantity, nextLevelOrders.getQuantity())) {
                            orderQuantity = orderQuantity.subtract(nextLevelOrders.getQuantity());
                            ordersToRemove.add(nextLevelOrders);
                            Order newOrderWithQuantityDelta = new Order(order.getType(), orderPrice, orderQuantity);
                            DebugLogger.log(" | MATCH | Full price and partial quantity match found between " + order + " and " + nextLevelOrders);
                            order = newOrderWithQuantityDelta;
                            matchingOrders.add(nextLevelOrders);
                        } else {
                            break;
                        }
                    }
                }
            } else {
                orderBook.addOrder(order);
            }
        }

        if (greaterThan(orderQuantity, BigDecimal.ZERO)) {
            orderBook.addOrder(new Order(order.getType(), order.getPrice(), orderQuantity));
        }
        return matchingOrders;
    }

    private void removeOrders(Set<Order> ordersToRemove) {
        ordersToRemove.forEach(orderBook::removeOrder);
    }


}
