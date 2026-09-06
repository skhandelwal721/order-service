package com.northwind.order.order;

import com.northwind.order.coupon.CouponClient;
import com.northwind.order.coupon.RedemptionView;
import com.northwind.order.fulfilment.ReleaseGate;
import com.northwind.order.pricing.OrderTotalCalculator;
import com.northwind.order.receipt.ReceiptTemplateSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Places an order.
 *
 * <p>On the discounted path the sequence is: redeem the coupon through coupon-service — which
 * charges the card on our behalf — then price, pick the receipt, and decide whether to release.
 *
 * <p>Every one of those last three steps reads a field off the redemption receipt, and none of
 * them can tell the difference between a receipt that is right and one whose fields have
 * changed meaning. See {@code docs/dependencies.md}.
 */
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final CouponClient couponClient;
    private final OrderTotalCalculator totalCalculator;
    private final ReceiptTemplateSelector receiptTemplateSelector;
    private final ReleaseGate releaseGate;

    public OrderService(OrderRepository orderRepository,
                        CouponClient couponClient,
                        OrderTotalCalculator totalCalculator,
                        ReceiptTemplateSelector receiptTemplateSelector,
                        ReleaseGate releaseGate) {
        this.orderRepository = orderRepository;
        this.couponClient = couponClient;
        this.totalCalculator = totalCalculator;
        this.receiptTemplateSelector = receiptTemplateSelector;
        this.releaseGate = releaseGate;
    }

    public OrderConfirmation place(PlaceOrderRequest request) {
        Order order = orderRepository.require(request.orderId());

        RedemptionView redemption = null;
        if (request.couponCode() != null && !request.couponCode().isBlank()) {
            redemption = couponClient.redeem(
                    request.couponCode(), order.invoiceId(), request.cardNumber(), order.currency());
        }

        BigDecimal payable = totalCalculator.payableTotal(order.subtotal(), redemption);
        String receiptTemplate = receiptTemplateSelector.templateFor(redemption);
        boolean release = releaseGate.mayRelease(redemption);

        String state = release ? "RELEASED" : "AWAITING_PAYMENT";

        log.info("placed orderId={} payable={} template={} state={}",
                order.orderId(), payable, receiptTemplate, state);

        return new OrderConfirmation(
                order.orderId(),
                order.subtotal(),
                redemption == null ? BigDecimal.ZERO.setScale(2) : redemption.discount(),
                payable,
                receiptTemplate,
                state);
    }
}
