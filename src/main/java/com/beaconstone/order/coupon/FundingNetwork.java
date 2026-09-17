package com.beaconstone.order.coupon;

/**
 * The card network that funded a promotion, as reported by coupon-service.
 *
 * <p>Read from {@code fundingNetwork} on the redemption receipt. We use it to pick the receipt
 * template, because network-funded promotions carry the network's branding requirements — a
 * Visa-funded discount has to say so on the customer receipt.
 */
public enum FundingNetwork {

    VISA,
    MASTERCARD;

    /**
     * Resolves the funding network from a redemption receipt.
     *
     * <p>Returns {@code null} rather than throwing: an unrecognised value means we fall back to
     * the generic receipt template. That is a deliberate trade — a wrong-looking receipt is
     * better than a failed checkout — but it is also why a change here is invisible until
     * someone reads {@code ReceiptTemplateFallbackRate}.
     */
    public static FundingNetwork parseOrNull(String fundingNetwork) {
        if (fundingNetwork == null) {
            return null;
        }
        try {
            return FundingNetwork.valueOf(fundingNetwork);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
