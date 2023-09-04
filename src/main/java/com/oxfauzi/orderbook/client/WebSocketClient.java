package com.oxfauzi.orderbook.client;

import com.oxfauzi.orderbook.OrderPublisher;
import com.oxfauzi.orderbook.domain.Order;
import com.oxfauzi.orderbook.util.DebugLogger;

import java.net.http.WebSocket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;

import static com.oxfauzi.orderbook.domain.Constants.POISON_ORDER;

public class WebSocketClient implements WebSocket.Listener {

    private final CountDownLatch messageLatch;
    private final BlockingQueue<Order> orderQueue;
    private final String channelSubscriptionMessage;
    private final OrderPublisher orderPublisher;

    public WebSocketClient(BlockingQueue<Order> orderQueue,
                           CountDownLatch messageLatch,
                           String channelSubscriptionMessage,
                           OrderPublisher orderPublisher) {
        this.messageLatch = messageLatch;
        this.orderQueue = orderQueue;
        this.channelSubscriptionMessage = channelSubscriptionMessage;
        this.orderPublisher = orderPublisher;
    }

    @Override
    public void onOpen(WebSocket webSocket) {
        DebugLogger.log(" | CONNECTIVITY | Opening websocket connection...");
        webSocket.sendText(channelSubscriptionMessage, true);
        WebSocket.Listener.super.onOpen(webSocket);
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        final String message = data.toString();
        orderPublisher.publishOrder(message, last);
        return WebSocket.Listener.super.onText(webSocket, data, last);
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        DebugLogger.log(" | CONNECTIVITY | WebSocket connection closed");
        sendPoisonMessage();
        return WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        DebugLogger.log(" | CONNECTIVITY | WebSocket error: " + error.toString());
        sendPoisonMessage();
        WebSocket.Listener.super.onError(webSocket, error);
    }

    private void sendPoisonMessage() {
        try {
            orderQueue.put(POISON_ORDER);
            messageLatch.countDown();
        } catch (InterruptedException ex) {
            DebugLogger.log(" | CONNECTIVITY | Interrupted");
            Thread.currentThread().interrupt();
        }
    }

}
