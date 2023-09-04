package com.oxfauzi.orderbook;

import com.oxfauzi.orderbook.domain.Order;
import com.oxfauzi.orderbook.util.DebugLogger;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

import static com.oxfauzi.orderbook.domain.Constants.POISON_ORDER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderPublisherTest {

    private static final String singleUpdateMessage = """
            {
              "type": "l2update",
              "product_id": "BTC-USD",
              "time": "2019-08-14T20:42:27.265Z",
              "changes": [
                [
                  "buy",
                  "10101.80000000",
                  "0.162567"
                ]
              ]
            }""";
    private static final String multiUpdateMessage = """
            {
              "type": "l2update",
              "product_id": "BTC-USD",
              "changes": [
                [
                  "buy",
                  "22356.270000",
                  "0.00000000"
                ],
                [
                  "buy",
                  "22356.300000",
                  "1.00000000"
                ]
              ],
              "time": "2022-08-04T15:25:05.010758Z"
            }""";

    private static Order order1 = new Order("buy", BigDecimal.valueOf(22356.27), BigDecimal.valueOf(0.0));
    private static Order order2 = new Order("buy", BigDecimal.valueOf(22356.3), BigDecimal.valueOf(1.0));
    private static Order singleOrder = new Order("buy", BigDecimal.valueOf(10101.8), BigDecimal.valueOf(0.162567));
    @Mock
    private CountDownLatch messageLatch;

    @Mock
    private BlockingQueue<Order> orderQueue;

    private OrderPublisher orderPublisher;
    private StringBuilder stringBuilder;

    @BeforeAll
    static void before() {
        DebugLogger.initializeForTest();
    }

    @BeforeEach
    void setup() {
        stringBuilder = new StringBuilder();
        orderPublisher = new OrderPublisher(messageLatch, orderQueue, stringBuilder);
    }

    @Test
    void givenMessageIsNotLast_whenPublishIsCalled_accumulateMessage() {

        orderPublisher.publishOrder("ABC", false);
        orderPublisher.publishOrder("DEF", false);
        orderPublisher.publishOrder("GHI", false);

        String result = stringBuilder.toString();

        assertThat(result).isEqualTo("ABCDEFGHI");
        verifyNoInteractions(orderQueue);
        verifyNoInteractions(messageLatch);
    }

    @Test
    void givenMessageIsLast_whenMessageIsNotAnUpdateMessage_messageIsIgnored() {

        orderPublisher.publishOrder("ABC", false);
        orderPublisher.publishOrder("DEF", false);
        orderPublisher.publishOrder("GHI", true);

        String result = stringBuilder.toString();

        assertThat(result).isBlank();
        verifyNoInteractions(orderQueue);
        verifyNoInteractions(messageLatch);

    }

    @Test
    void givenMessageIsAMultiUpdate_whenMessageIsLast_createMultipleOrdersAndPublish() throws InterruptedException {

        orderPublisher.publishOrder(multiUpdateMessage, true);

        String result = stringBuilder.toString();

        assertThat(result).isBlank();
        ArgumentCaptor<Order> argumentCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderQueue, times(2)).put(argumentCaptor.capture());

        List<Order> allOrdersCreated = argumentCaptor.getAllValues();
        assertThat(allOrdersCreated).containsExactly(order1, order2);
    }

    @Test
    void givenMessageIsASingleUpdate_whenMessageIsLast_createAnOrderAndPublish() throws InterruptedException {

        orderPublisher.publishOrder(singleUpdateMessage, true);

        String result = stringBuilder.toString();

        assertThat(result).isBlank();
        ArgumentCaptor<Order> argumentCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderQueue, times(1)).put(argumentCaptor.capture());

        List<Order> allOrdersCreated = argumentCaptor.getAllValues();
        assertThat(allOrdersCreated).containsExactly(singleOrder);
    }

    @Test
    void givenAnExceptionThrown_sendPoisonMessage() throws InterruptedException {

        doThrow(IllegalStateException.class).when(orderQueue).put(any(Order.class));

        assertThrows(IllegalStateException.class, () -> orderPublisher.publishOrder(singleUpdateMessage, true));

        ArgumentCaptor<Order> argumentCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderQueue, times(2)).put(argumentCaptor.capture());
        assertThat(argumentCaptor.getAllValues()).containsExactly(singleOrder, POISON_ORDER);
        verify(messageLatch, times(1)).countDown();
    }
}