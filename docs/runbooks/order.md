# Runbook — place order

## SLO

| Endpoint | SLO |
| --- | --- |
| `POST /v1/orders` | p99 < 2.5s, 99.99% availability |

## Alarms

| Alarm | Means |
| --- | --- |
| `OrderErrorRate` | Checkout is failing. On the discounted path this is almost always `coupon-service` |
| `OrdersStuckUnreleased` | Redemption receipts are arriving with a status we do not recognise as paid. Check whether `coupon-service` added a status value |
| `ReceiptTemplateFallbackRate` | `fundingNetwork` is not resolving. Receipts are rendering without scheme branding — a contractual breach, not an outage |

## What we have no alarm for

**A wrong order total.** `OrderTotalCalculator` does no sanity check on `discount`, so a change
in what that field means produces arithmetically valid, financially wrong prices with no error
and no metric. If someone reports being charged the wrong amount after a promotion, check
`coupon-service`'s redemption contract against our pinned `coupon.contract.version` **first** —
before looking anywhere in this service.

## Rollback

Rolling this service back does not refund a customer who was overcharged. If a discount-semantics
change has shipped upstream, the affected orders need identifying and refunding by hand for the
whole window, regardless of what we deploy here.
