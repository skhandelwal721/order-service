package com.beaconstone.order.coupon;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Contract test for coupon-service's redemption receipt, taken from their
 * {@code docs/api/redemption.md}.
 */
class RedemptionViewTest {

    private final ObjectMapper mapper = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private static final String PUBLISHED_RECEIPT = """
            {
              "redemptionId": "rdm_1c9f4a70",
              "couponCode": "NW-VISA-10",
              "chargeId": "chg_9f3b7c21",
              "fundingNetwork": "VISA",
              "discount": "10.00",
              "status": "REDEEMED"
            }
            """;

    @Test
    void deserializesThePublishedReceipt() throws Exception {
        RedemptionView view = mapper.readValue(PUBLISHED_RECEIPT, RedemptionView.class);

        assertEquals("VISA", view.fundingNetwork());
        assertEquals(new BigDecimal("10.00"), view.discount());
        assertEquals("REDEEMED", view.status());
    }

    /**
     * Documents the lenient trade. Extra fields are ignored — checkout stays up — and a field
     * that disappears becomes {@code null} rather than an error. Both are silent.
     */
    @Test
    void ignoresUnknownFieldsAndNullsMissingOnes() throws Exception {
        String changed = """
                {
                  "redemptionId": "rdm_1c9f4a70",
                  "couponCode": "NW-VISA-10",
                  "chargeId": "chg_9f3b7c21",
                  "fundingScheme": "VISA_CREDIT",
                  "discount": "10.00",
                  "discountBasis": "PERCENT",
                  "status": "REDEEMED"
                }
                """;

        RedemptionView view = mapper.readValue(changed, RedemptionView.class);

        assertNull(view.fundingNetwork(), "renamed field reads as null, with no error");
        assertEquals(new BigDecimal("10.00"), view.discount(),
                "discount still parses — we cannot see that it now means a percentage");
    }
}
