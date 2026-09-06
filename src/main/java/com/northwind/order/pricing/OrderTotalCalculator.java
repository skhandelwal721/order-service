package com.northwind.order.pricing;

import com.northwind.order.coupon.RedemptionView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Prices an order after a coupon has been redeemed.
 *
 * <p><strong>{@code discount} is an absolute currency amount.</strong> coupon-service documents
 * it that way ({@code docs/api/redemption.md}) and we subtract it directly from the order
 * subtotal. A receipt reporting {@code "discount": "10.00"} on a 249.00 order means the
 * customer pays 239.00.
 *
 * <p>There is no sanity check on the magnitude, and that is the dangerous part. If the field
 * ever stopped being an absolute amount — carrying a percentage, or a basis-point figure, or an
 * amount in minor units — every number here would still be arithmetically valid. Nothing
 * throws. Nothing is logged. The customer is simply charged an amount that does not match the
 * discount they were promised, on every discounted order, until someone reconciles the
 * storefront against the promotion ledger.
 *
 * <p>We cannot defend against that from here: an absolute amount and a percentage are both
 * just decimals. It has to hold on the producing side.
 */
@Component
public class OrderTotalCalculator {

    private static final Logger log = LoggerFactory.getLogger(OrderTotalCalculator.class);

    /**
     * The amount the customer actually pays.
     *
     * @param subtotal   the order subtotal before any discount
     * @param redemption the receipt from coupon-service, or {@code null} for an undiscounted order
     */
    public BigDecimal payableTotal(BigDecimal subtotal, RedemptionView redemption) {
        if (redemption == null) {
            return subtotal.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal discount = redemption.discount();
        BigDecimal payable = subtotal.subtract(discount).setScale(2, RoundingMode.HALF_UP);

        log.info("priced order subtotal={} discount={} payable={} redemptionId={}",
                subtotal, discount, payable, redemption.redemptionId());

        return payable.max(BigDecimal.ZERO.setScale(2));
    }
}
