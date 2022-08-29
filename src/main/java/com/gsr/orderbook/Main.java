package com.gsr.orderbook;

import com.gsr.orderbook.client.WebSocketClient;
import com.gsr.orderbook.command.level.AddLevelCommand;
import com.gsr.orderbook.command.level.UpdateLevelCommand;
import com.gsr.orderbook.command.order.AddOrderCommand;
import com.gsr.orderbook.command.order.RemoveOrderCommand;
import com.gsr.orderbook.domain.Level;
import com.gsr.orderbook.domain.Order;
import com.gsr.orderbook.domain.OrderBook;
import com.gsr.orderbook.domain.OrderBookEntry;
import com.gsr.orderbook.matching.DefaultOrderMatchingAlgorithm;
import com.gsr.orderbook.matching.OrderMatcher;
import com.gsr.orderbook.util.DebugLogger;
import picocli.CommandLine;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.file.FileSystems;
import java.util.*;
import java.util.concurrent.*;

import static com.gsr.orderbook.domain.Constants.POISON_ORDER;

@CommandLine.Command(name = "order-book")
public class Main implements Runnable {

    private static final String SUBSCRIPTION_MESSAGE = """
            {
             "type": "subscribe",
             "product_ids": ["<currencyPair>"],
             "channels": ["level2"]
            }""";

    @CommandLine.Option(names = {"-c", "--currencyPair"}, required = true, description = "Currency pair to get data for. Eg. ETH-USD, BTC-USD, BTC-GBP")
    private String currencyPair;

    @Override
    public void run() {
        final BlockingQueue<Order> orderQueue = new LinkedBlockingQueue<>(1);
        final CountDownLatch messageLatch = new CountDownLatch(1);

        DebugLogger.initialize();

        TreeMap<BigDecimal, Level> buyLevels = new TreeMap<>(Comparator.reverseOrder()); //highest price to lowest
        TreeMap<BigDecimal, Level> sellLevels = new TreeMap<>(Comparator.naturalOrder()); //lowest price to highest
        Map<Order, OrderBookEntry> orderEntries = new HashMap<>();
        RemoveOrderCommand removeOrderOperation = new RemoveOrderCommand(buyLevels, sellLevels, orderEntries);
        UpdateLevelCommand updateLevelCommand = new UpdateLevelCommand();
        AddLevelCommand addLevelCommand = new AddLevelCommand();
        AddOrderCommand addOrderOperation = new AddOrderCommand(updateLevelCommand, addLevelCommand, buyLevels, sellLevels, orderEntries);

        OrderBookPrinter orderBookPrinter = new OrderBookPrinter();

        OrderBook orderBook = new OrderBook(buyLevels, sellLevels, orderEntries, addOrderOperation, removeOrderOperation, orderBookPrinter);
        OrderMatcher orderMatcher = new OrderMatcher(new DefaultOrderMatchingAlgorithm(orderBook, buyLevels, sellLevels));
        OrderPublisher orderPublisher = new OrderPublisher(messageLatch, orderQueue, new StringBuilder());

        final OrderBookMaintainer orderBookMaintainer = new OrderBookMaintainer(orderQueue, orderBook, orderMatcher, messageLatch);

        String CURRENCY_PAIR_CONSTANT = "<currencyPair>";
        String channelSubscriptionMessage = SUBSCRIPTION_MESSAGE.replaceAll(CURRENCY_PAIR_CONSTANT, currencyPair);

        String EXCHANGE_ENDPOINT = "wss://ws-feed.exchange.coinbase.com";
        WebSocket ws = HttpClient
                .newHttpClient()
                .newWebSocketBuilder()
                .buildAsync(URI.create(EXCHANGE_ENDPOINT), new WebSocketClient(orderQueue, messageLatch, channelSubscriptionMessage, orderPublisher))
                .join();

        CompletableFuture.runAsync(orderBookMaintainer);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                DebugLogger.log(" | MAIN |Received keyboard interrupt, shutting down...");
                orderQueue.put(POISON_ORDER);
                ws.sendClose(WebSocket.NORMAL_CLOSURE, "");
                ws.abort();
            } catch (InterruptedException e) {
                DebugLogger.log(" | MAIN | Interrupted");
                Thread.currentThread().interrupt();
            }
        }));

        try {
            messageLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) {
        String logFile = FileSystems.getDefault()
                .getPath("")
                .toAbsolutePath() + FileSystems.getDefault().getSeparator();
        System.out.println("Waiting to get 10 price levels before printing order book...");
        System.out.println("Debug information will be written to: " + logFile + " filename = order-book-logs_{{time}}.txt");
        new CommandLine(new Main()).execute(args);
        System.exit(1);
    }
}
