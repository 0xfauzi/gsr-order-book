package com.gsr.orderbook.domain;

import com.fasterxml.jackson.annotation.*;
import com.gsr.orderbook.util.BigDecimalUtils;

import java.math.BigDecimal;
import java.util.Objects;

@JsonFormat(shape = JsonFormat.Shape.ARRAY)
@JsonPropertyOrder({"type", "price", "quantity"})
public class Order {

    private static final double EPSILON = 0.0000000001d;

    private final String type;

    private final BigDecimal price;


    private final BigDecimal quantity;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public Order(@JsonProperty("type") String type,
                 @JsonProperty("price") BigDecimal price,
                 @JsonProperty("quantity") BigDecimal quantity) {
        this.type = type;
        this.price = price;
        this.quantity = quantity;
    }

    public boolean isBuy() {
        return "buy".equals(type);
    }

    public boolean isCancelOrder() {
        return BigDecimalUtils.equal(BigDecimal.ZERO, quantity);
    }

    public String getType() {
        return type;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return BigDecimalUtils.equal(order.price, price)&& BigDecimalUtils.equal(order.quantity, quantity) && type.equals(order.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, price, quantity);
    }

    @Override
    public String toString() {
        return "Order{" +
                "type='" + type + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                '}';
    }
}
