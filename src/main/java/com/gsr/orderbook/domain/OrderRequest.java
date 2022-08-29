package com.gsr.orderbook.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record OrderRequest(@JsonProperty String type,
                           @JsonProperty("product_id") String productId,
                           @JsonProperty List<Order> changes,
                           @JsonProperty String time) {

    @Override
    public String toString() {
        return "Update{" +
                " productId='" + productId + '\'' +
                ", orders=" + changes +
                '}';
    }
}
