package com.northwind.order.order;

import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@Repository
public class OrderRepository {

    private static final Map<String, Order> ORDERS = Map.of(
            "ord-5001", new Order("ord-5001", "cust-77", "inv-1001",
                    new BigDecimal("249.00"), "GBP", "PENDING"),
            "ord-5002", new Order("ord-5002", "cust-88", "inv-1002",
                    new BigDecimal("18.50"), "GBP", "PENDING"));

    public Optional<Order> find(String orderId) {
        return Optional.ofNullable(ORDERS.get(orderId));
    }

    public Order require(String orderId) {
        return find(orderId).orElseThrow(() -> new IllegalArgumentException("no such order: " + orderId));
    }
}
