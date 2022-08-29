package com.gsr.orderbook.command.order;

import com.gsr.orderbook.domain.Order;

public interface OrderBookCommand {

    void execute(Order order);
}
