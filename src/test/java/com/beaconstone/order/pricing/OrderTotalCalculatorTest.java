package com.beaconstone.order.pricing;

import com.beaconstone.order.coupon.RedemptionView;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pins the assumption the storefront price rests on: {@code discount} is an absolute currency
 * amount, not a percentage.
 */
class OrderTotalCalculatorTest {

    private final OrderTotalCalculator calculator = new OrderTotalCalculator();

    @Test
    void subtractsTheDiscountAsAnAbsoluteAmount() {
        BigDecimal payable = calculator.payableTotal(
                new BigDecimal("249.00"), redemption("10.00"));

        assertEquals(new BigDecimal("239.00"), payable);
    }

    @Test
    void anUndiscountedOrderPaysTheSubtotal() {
        assertEquals(new BigDecimal("249.00"),
                calculator.payableTotal(new BigDecimal("249.00"), null));
    }

    /**
     * Documents the failure mode we cannot detect.
     *
     * <p>If {@code discount} ever carried a percentage, {@code "10.00"} would mean 10% — 24.90
     * on this order, not 10.00. This test asserts what we would compute in that case, which is
     * 239.00: a customer promised 10% off pays 14.90 too much and nothing errors.
     *
     * <p>It is here so that the number is written down, not because the behaviour is correct.
     */
    @Test
    void aPercentageInThisFieldWouldSilentlyOvercharge() {
        BigDecimal weWouldCharge = calculator.payableTotal(
                new BigDecimal("249.00"), redemption("10.00"));
        BigDecimal customerWasPromised = new BigDecimal("224.10"); // 249.00 less 10%

        assertEquals(new BigDecimal("239.00"), weWouldCharge);
        assertEquals(new BigDecimal("14.90"), weWouldCharge.subtract(customerWasPromised));
    }

    private static RedemptionView redemption(String discount) {
        return new RedemptionView("rdm_1", "NW-VISA-10", "chg_1",
                "VISA", new BigDecimal(discount), "REDEEMED");
    }
}
