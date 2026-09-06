package com.northwind.order.fulfilment;

import com.northwind.order.coupon.RedemptionView;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReleaseGateTest {

    private final ReleaseGate gate = new ReleaseGate();

    @Test
    void releasesOnRedeemed() {
        assertTrue(gate.mayRelease(redemption("REDEEMED")));
    }

    @Test
    void holdsOnAnythingElse() {
        assertFalse(gate.mayRelease(redemption("HELD")));
        assertFalse(gate.mayRelease(redemption("FAILED")));
    }

    /**
     * The closed set. A status coupon-service adds later reads as "not paid" here, so
     * fulfilment stops for those orders and the only symptom is a rising unreleased count.
     */
    @Test
    void treatsAnUnknownStatusAsUnpaid() {
        assertFalse(gate.mayRelease(redemption("REDEEMED_PARTIAL")));
    }

    private static RedemptionView redemption(String status) {
        return new RedemptionView("rdm_1", "NW-VISA-10", "chg_1",
                "VISA", new BigDecimal("10.00"), status);
    }
}
