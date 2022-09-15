package com.gsr.orderbook.domain;

import com.gsr.orderbook.util.BigDecimalUtils;

import java.math.BigDecimal;
import java.util.*;

public class Level {

    private final BigDecimal price;
    private OrderBookEntry head;
    private OrderBookEntry tail;

    public Level(BigDecimal price) {
        this.price = price;
        this.head = null;
        this.tail = null;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public OrderBookEntry getHead() {
        return head;
    }

    public OrderBookEntry getTail() {
        return tail;
    }

    public void setHead(OrderBookEntry head) {
        this.head = head;
    }

    public void setTail(OrderBookEntry tail) {
        this.tail = tail;
    }

    public boolean isEmpty() {
        return head == null && tail == null;
    }

    public OrderType getType() {
        if (isEmpty()) {
            return OrderType.UNKNOWN;
        } else {
            return head.getCurrentOrder().isBuy() ? OrderType.BUY : OrderType.SELL;
        }
    }

    public int getLevelOrderCount() {
        if (isEmpty()) return 0;

        OrderBookEntry headPointer = head;
        int orderCount = 0;
        while (headPointer != null) {
            final boolean greaterThanZero = BigDecimalUtils.greaterThan(headPointer.getCurrentOrder().getQuantity(), BigDecimal.ZERO);
            if (greaterThanZero) {
                orderCount++;
            }
            headPointer = headPointer.getNext();
        }
        return orderCount;
    }

    public BigDecimal getLevelOrderQuantity() {
        OrderBookEntry headPointer = head;
        BigDecimal orderQuantity = BigDecimal.ZERO;
        while (headPointer != null) {
            orderQuantity = orderQuantity.add(headPointer.getCurrentOrder().getQuantity());
            headPointer = headPointer.getNext();
        }
        return orderQuantity;
    }

    public List<Order> getOrders() {
        final List<Order> orders = new ArrayList<>();
        OrderBookEntry headPointer = head;
        while (headPointer != null) {
            if (BigDecimalUtils.greaterThan(headPointer.getCurrentOrder().getQuantity(), BigDecimal.ZERO)) {
                orders.add(headPointer.getCurrentOrder());
            }
            headPointer = headPointer.getNext();
        }
        return Collections.unmodifiableList(orders);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Level level = (Level) o;
        return BigDecimalUtils.equal(level.price, price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(price);
    }

    @Override
    public String toString() {
        return "Level{" +
                " price=" + price +
                " type=" + getType() +
                " orderCount=" + getLevelOrderCount() +
                " orderQuantity=" + getLevelOrderQuantity() +
                " orders=" + Arrays.deepToString(getOrders().toArray()) +
                '}';
    }
}
