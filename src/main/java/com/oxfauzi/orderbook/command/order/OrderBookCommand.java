package com.oxfauzi.orderbook.command.order;

import com.oxfauzi.orderbook.domain.Order;

public interface OrderBookCommand {

    void execute(Order order);
}
