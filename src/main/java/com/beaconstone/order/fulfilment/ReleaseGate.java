package com.beaconstone.order.fulfilment;

import com.beaconstone.order.coupon.RedemptionView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Decides whether an order may be released to the warehouse.
 *
 * <p>An order is released only when the money is in. On the discounted path that means the
 * redemption receipt says {@code REDEEMED} — coupon-service documents that value as meaning
 * the discount was applied <em>and</em> the charge settled.
 *
 * <p><strong>Any other status holds the order.</strong> That is the safe direction — we would
 * rather delay a parcel than ship goods we were not paid for — but it is a closed set: a status
 * value we do not recognise is treated as "not paid", so a new status on the producing side
 * silently stops fulfilment. Orders pile up in {@code AWAITING_PAYMENT} and the only signal is
 * {@code OrdersStuckUnreleased}.
 */
@Component
public class ReleaseGate {

    /** coupon-service's terminal success status. The only value that releases an order. */
    static final String REDEEMED = "REDEEMED";

    private static final Logger log = LoggerFactory.getLogger(ReleaseGate.class);

    public boolean mayRelease(RedemptionView redemption) {
        if (redemption == null) {
            // Undiscounted orders are charged elsewhere and released by their own gate.
            return true;
        }

        boolean paid = REDEEMED.equals(redemption.status());

        if (!paid) {
            log.warn("holding order — redemptionId={} status={} is not {}",
                    redemption.redemptionId(), redemption.status(), REDEEMED);
        }

        return paid;
    }
}
