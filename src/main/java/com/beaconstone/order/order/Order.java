package com.beaconstone.order.order;

import java.math.BigDecimal;

public record Order(
        String orderId,
        String customerId,
        String invoiceId,
        BigDecimal subtotal,
        String currency,
        String state
) {
}
