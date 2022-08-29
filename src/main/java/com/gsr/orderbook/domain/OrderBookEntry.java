package com.gsr.orderbook.domain;

import java.util.Objects;

public class OrderBookEntry {

    private final Order currentOrder;
    private final Level level;
    private OrderBookEntry next;
    private OrderBookEntry previous;

    public OrderBookEntry(Order currentOrder, Level level) {
        this.currentOrder = currentOrder;
        this.level = level;
        this.next = null;
        this.previous = null;
    }

    public void setNext(OrderBookEntry next) {
        this.next = next;
    }

    public void setPrevious(OrderBookEntry previous) {
        this.previous = previous;
    }

    public Order getCurrentOrder() {
        return currentOrder;
    }

    public Level getLevel() {
        return level;
    }

    public OrderBookEntry getNext() {
        return next;
    }

    public OrderBookEntry getPrevious() {
        return previous;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderBookEntry that = (OrderBookEntry) o;
        return currentOrder.equals(that.currentOrder) && level.equals(that.level);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currentOrder, level);
    }
}
