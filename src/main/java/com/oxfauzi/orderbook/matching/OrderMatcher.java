package com.oxfauzi.orderbook.matching;

import com.oxfauzi.orderbook.domain.Order;

import java.util.List;

public class OrderMatcher {

    private final MatchingAlgorithm matchingAlgorithm;

    public OrderMatcher(MatchingAlgorithm matchingAlgorithm) {
        this.matchingAlgorithm = matchingAlgorithm;
    }

    public List<Order> matchOrders(Order order) {
        return matchingAlgorithm.matchOrders(order);
    }

}
