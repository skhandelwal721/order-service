package com.northwind.order.order;

import jakarta.validation.constraints.NotBlank;

public record PlaceOrderRequest(

        @NotBlank(message = "orderId is required")
        String orderId,

        @NotBlank(message = "cardNumber is required")
        String cardNumber,

        /** Optional. An order without a coupon skips coupon-service entirely. */
        String couponCode
) {
}
