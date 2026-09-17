package com.beaconstone.order.receipt;

import com.beaconstone.order.coupon.RedemptionView;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReceiptTemplateSelectorTest {

    private final ReceiptTemplateSelector selector = new ReceiptTemplateSelector();

    @Test
    void picksTheVisaFundedTemplate() {
        assertEquals("receipt/visa-funded", selector.templateFor(redemption("VISA")));
    }

    @Test
    void picksTheMastercardFundedTemplate() {
        assertEquals("receipt/mastercard-funded", selector.templateFor(redemption("MASTERCARD")));
    }

    /**
     * The fallback that makes a contract change invisible. An absent or unrecognised
     * {@code fundingNetwork} renders a generic receipt — no error, no exception, and we are
     * quietly out of compliance with the scheme branding agreements.
     */
    @Test
    void fallsBackToGenericWhenTheFundingNetworkIsNotRecognised() {
        assertEquals(ReceiptTemplateSelector.GENERIC, selector.templateFor(redemption(null)));
        assertEquals(ReceiptTemplateSelector.GENERIC, selector.templateFor(redemption("VISA_CREDIT")));
    }

    private static RedemptionView redemption(String fundingNetwork) {
        return new RedemptionView("rdm_1", "NW-VISA-10", "chg_1",
                fundingNetwork, new BigDecimal("10.00"), "REDEEMED");
    }
}
