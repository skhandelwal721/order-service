package com.northwind.order.coupon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Redeems a coupon through coupon-service.
 *
 * <p><strong>Hard dependency on the discounted path.</strong> A discounted order cannot be
 * priced or released without a redemption receipt, so a failure here fails the checkout rather
 * than quietly charging full price.
 *
 * <p>We call {@code POST /v1/redemptions}. coupon-service performs the card charge itself, via
 * billing-service — we never talk to billing-service directly, which means we also cannot see
 * how it is being charged on our behalf.
 */
@Component
public class CouponClient {

    private static final Logger log = LoggerFactory.getLogger(CouponClient.class);

    private final String baseUrl;
    private final String redemptionPath;

    public CouponClient(@Value("${clients.coupon.baseUrl}") String baseUrl,
                        @Value("${clients.coupon.redemptionPath}") String redemptionPath) {
        this.baseUrl = baseUrl;
        this.redemptionPath = redemptionPath;
    }

    public RedemptionView redeem(String couponCode, String invoiceId, String cardNumber, String currency) {
        log.info("redeeming couponCode={} invoiceId={} url={}",
                couponCode, invoiceId, baseUrl + redemptionPath);

        // Stubbed for the fixture: the real client POSTs and deserializes into RedemptionView
        // with the lenient ObjectMapper configured in application.yml.
        return new RedemptionView(
                "rdm_1c9f4a70",
                couponCode,
                "chg_9f3b7c21",
                "VISA",
                new BigDecimal("10.00"),
                "REDEEMED");
    }
}
