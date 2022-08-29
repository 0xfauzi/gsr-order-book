package com.gsr.orderbook.matching;

import com.gsr.orderbook.domain.Order;
import com.gsr.orderbook.matching.MatchingAlgorithm;

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
