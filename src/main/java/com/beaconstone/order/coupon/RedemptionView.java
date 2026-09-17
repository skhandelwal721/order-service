package com.beaconstone.order.coupon;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

/**
 * Our view of a coupon-service redemption receipt.
 *
 * <p>Hand-written against coupon-service {@code docs/api/redemption.md} at the version pinned
 * in {@code pom.xml} as {@code coupon.contract.version}.
 *
 * <p><strong>Deliberately lenient.</strong> {@code ignoreUnknown = true} — coupon-service ships
 * additive fields regularly and we are on the storefront path, so we would rather ignore a
 * field we do not know than fail a customer's checkout.
 *
 * <p>The cost of that choice is worth stating plainly, because it is the opposite trade from
 * the one coupon-service made with us: <strong>nothing here fails loudly.</strong> If a field
 * is added we ignore it. If a field keeps its name and type but changes what it <em>means</em>,
 * we keep reading it and we keep being wrong. There is no exception, no alarm, and no
 * deserialization error — just an order total that does not match what the customer was
 * shown.
 *
 * <p>What we depend on, all documented as stable by coupon-service:
 *
 * <ol>
 *   <li>{@code discount} is an <strong>absolute currency amount</strong> in the order's
 *       currency — see {@code OrderTotalCalculator}.</li>
 *   <li>{@code fundingNetwork} is a card network name — see {@code ReceiptTemplateSelector}.</li>
 *   <li>{@code status} is {@code REDEEMED} when, and only when, the discount was applied and
 *       the charge settled — see {@code ReleaseGate}.</li>
 * </ol>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RedemptionView(
        String redemptionId,
        String couponCode,
        String chargeId,
        String fundingNetwork,
        BigDecimal discount,
        String status
) {
}
