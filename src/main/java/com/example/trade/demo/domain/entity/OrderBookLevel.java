package com.example.trade.demo.domain.entity;

import java.math.BigDecimal;

public class OrderBookLevel {
    private final BigDecimal price;
    private final BigDecimal quantity;

    public OrderBookLevel(BigDecimal price, BigDecimal quantity) {
        this.price = price;
        this.quantity = quantity;
    }

    public BigDecimal getPrice() { return price; }
    public BigDecimal getQuantity() { return quantity; }

    @Override
    public String toString() {
        return String.format("Level{price=%s, qty=%s}", price, quantity);
    }
}
