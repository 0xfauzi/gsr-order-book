package com.oxfauzi.orderbook.matching;

import com.oxfauzi.orderbook.domain.Order;

import java.util.List;

public interface MatchingAlgorithm {

    List<Order> matchOrders(Order order);
}
