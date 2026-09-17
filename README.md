# order-service

Storefront order orchestration for Beacon Stone Retail. Tier 1 — this is the checkout button.

## Responsibilities

- `POST /v1/orders` — places an order. On the discounted path, redeems the coupon through
  `coupon-service` (which charges the card on our behalf), then prices the order, picks the
  receipt template, and decides whether to release to the warehouse.

## Position in the checkout chain

```
order-service  ──▶  coupon-service  ──▶  billing-service
 (this service)      applies discount      takes the money
                     and charges
```

We never call `billing-service` directly. `coupon-service` charges on our behalf, which means
**we cannot see how the card is being charged** — which endpoint, with what guards, at what
amount. We see only the receipt that comes back.

## Dependencies

| Service | Used for | Criticality |
| --- | --- | --- |
| `coupon-service` | redeeming the coupon and charging the discounted invoice | hard on the discounted path — we fail checkout rather than ship unpaid goods |

What we read from the redemption receipt, and where:

| We read | We use it for | Breaks if |
| --- | --- | --- |
| `discount` | subtracting from the order subtotal as an **absolute currency amount** — [`OrderTotalCalculator`](src/main/java/com/beaconstone/order/pricing/OrderTotalCalculator.java) | it ever carries anything other than an absolute amount |
| `fundingNetwork` | picking the receipt template, since network-funded promotions carry scheme branding requirements — [`ReceiptTemplateSelector`](src/main/java/com/beaconstone/order/receipt/ReceiptTemplateSelector.java) | the field is renamed or its values change |
| `status` | releasing the order only when the money is in — [`ReleaseGate`](src/main/java/com/beaconstone/order/fulfilment/ReleaseGate.java) | a new status value is introduced |

See [`docs/dependencies.md`](docs/dependencies.md).

## Why this service fails *open*, and what that costs

`coupon-service` chose to fail closed: it holds a redemption it cannot reconcile. We chose the
opposite. [`RedemptionView`](src/main/java/com/beaconstone/order/coupon/RedemptionView.java) is
lenient, the receipt selector falls back, and the total calculator does no sanity check.

That keeps checkout up when `coupon-service` ships an additive change. It also means **none of
our three dependencies fail loudly.** A field that keeps its name and type but changes what it
means is not an error here — it is an order total that quietly does not match what the customer
was shown, on every discounted order, until someone reconciles the storefront against the
promotion ledger.

Both choices are defensible. Together they mean one upstream change can take `coupon-service`
down loudly and corrupt our prices silently, and only one of those has an alarm.

## Local development

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```
