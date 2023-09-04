package com.oxfauzi.orderbook.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class OrderRequest {

    private final String type;
    private final String requestId;
    private final String productId;
    private final List<Order> changes;
    private final String time;

    public OrderRequest(@JsonProperty("type") String type,
                        @JsonProperty("product_id") String productId,
                        @JsonProperty("changes") List<Order> changes,
                        @JsonProperty("time") String time) {
        this.type = type;
        this.requestId = UUID.randomUUID().toString();
        this.productId = productId;
        this.changes = changes;
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getProductId() {
        return productId;
    }

    public List<Order> getChanges() {
        return Collections.unmodifiableList(changes);
    }

    public String getTime() {
        return time;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (OrderRequest) obj;
        return Objects.equals(this.type, that.type) &&
                Objects.equals(this.requestId, that.requestId) &&
                Objects.equals(this.productId, that.productId) &&
                Objects.equals(this.time, that.time);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, requestId, productId, time);
    }

    @Override
    public String toString() {
        return "Update{" +
                " requestId='" + requestId + '\'' +
                " time='" + time + '\'' +
                " productId='" + productId + '\'' +
                ", orders=" + changes +
                '}';
    }

}
