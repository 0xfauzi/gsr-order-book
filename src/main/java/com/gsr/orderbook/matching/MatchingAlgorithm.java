package com.gsr.orderbook.matching;

import com.gsr.orderbook.domain.Order;

import java.util.List;

public interface MatchingAlgorithm {

    List<Order> matchOrders(Order order);
}
