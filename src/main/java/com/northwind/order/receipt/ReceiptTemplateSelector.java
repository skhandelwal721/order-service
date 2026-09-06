package com.northwind.order.receipt;

import com.northwind.order.coupon.FundingNetwork;
import com.northwind.order.coupon.RedemptionView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Picks the customer receipt template.
 *
 * <p>Network-funded promotions carry the funding network's branding requirements, so the
 * template is chosen from {@code fundingNetwork} on the redemption receipt.
 *
 * <p><strong>Falls back rather than failing.</strong> An unrecognised or absent
 * {@code fundingNetwork} yields {@link #GENERIC}, which renders a correct receipt with no
 * network branding. That keeps checkout up, and it means a contract change here shows up only
 * as a rising {@code ReceiptTemplateFallbackRate} — never as an error. If the network is
 * missing for every order, we are in breach of the scheme branding agreements and nothing in
 * this service will say so.
 */
@Component
public class ReceiptTemplateSelector {

    public static final String GENERIC = "receipt/generic";

    private static final Logger log = LoggerFactory.getLogger(ReceiptTemplateSelector.class);

    public String templateFor(RedemptionView redemption) {
        if (redemption == null) {
            return GENERIC;
        }

        FundingNetwork network = FundingNetwork.parseOrNull(redemption.fundingNetwork());

        if (network == null) {
            log.warn("no recognised funding network on redemptionId={} fundingNetwork={} — using generic receipt",
                    redemption.redemptionId(), redemption.fundingNetwork());
            return GENERIC;
        }

        return switch (network) {
            case VISA -> "receipt/visa-funded";
            case MASTERCARD -> "receipt/mastercard-funded";
        };
    }
}
