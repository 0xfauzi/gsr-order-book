package com.oxfauzi.orderbook;

import com.oxfauzi.orderbook.domain.Order;
import com.oxfauzi.orderbook.domain.OrderBook;
import com.oxfauzi.orderbook.matching.OrderMatcher;
import com.oxfauzi.orderbook.util.DebugLogger;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

import static com.oxfauzi.orderbook.domain.Constants.POISON_ORDER;

public class OrderBookMaintainer implements Runnable {

    private final BlockingQueue<Order> updateQueue;
    private final OrderBook orderBook;
    private final OrderMatcher orderMatcher;
    private final CountDownLatch messageLatch;

    public OrderBookMaintainer(BlockingQueue<Order> updateQueue,
                               OrderBook orderBook,
                               OrderMatcher orderMatcher,
                               CountDownLatch messageLatch) {
        this.updateQueue = updateQueue;
        this.orderBook = orderBook;
        this.orderMatcher = orderMatcher;
        this.messageLatch = messageLatch;
    }

    @Override
    public void run() {

        while (true) {

            Order order;
            try {
                order = updateQueue.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                messageLatch.countDown();
                break;
            }

            try {
                if (order.equals(POISON_ORDER)) {
                    DebugLogger.log(" | MAIN | Poison pill received, order maintenance will stop.");
                    System.out.println("Poison pill received, order maintenance will stop.");
                    Thread.currentThread().interrupt();
                    messageLatch.countDown();
                    break;
                } else {
                    List<Order> orders = orderMatcher.matchOrders(order);
                    if (!orders.isEmpty()) {
                        DebugLogger.log(" | MATCH | " + order + " matched " + orders.size() + " orders.");
                        orders.forEach(o -> DebugLogger.log(o.toString()));
                    }
                }
            } catch (Exception e) {
                DebugLogger.log(" | MAIN | Exception: " + e.getMessage());
                messageLatch.countDown();
                break;
            }

            orderBook.printOrderBook();
        }
    }
}
