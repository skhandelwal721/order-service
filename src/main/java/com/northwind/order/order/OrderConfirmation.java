package com.northwind.order.order;

import java.math.BigDecimal;

public record OrderConfirmation(
        String orderId,
        BigDecimal subtotal,
        BigDecimal discountApplied,
        BigDecimal payableTotal,
        String receiptTemplate,
        String state
) {
}
