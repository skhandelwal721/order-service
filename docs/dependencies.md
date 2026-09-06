# What we depend on in coupon-service

`coupon-service` is a hard upstream on the discounted checkout path. This document exists so
that anyone changing the redemption receipt can see what it costs us.

## 1. `discount` is an absolute currency amount

**Where:** `OrderTotalCalculator.payableTotal`.

**Why we need it:** it is subtracted directly from the order subtotal to get the amount the
customer pays.

**If it stops being an absolute amount:** nothing fails. A percentage, a basis-point figure or
an amount in minor units all parse as a decimal and all subtract cleanly. A customer promised
10% off a 249.00 order is charged 239.00 instead of 224.10 — **14.90 too much, on every
discounted order.** There is no exception, no alarm, and no deserialization error. The first
signal is a customer complaint or a storefront-versus-ledger reconciliation.

This is the single most expensive dependency in this service and the one we can least defend
against, because an absolute amount and a percentage are both just decimals. It has to hold on
the producing side.

**What would make this safe:** a new field (`discountBasis`, `discountPercent`) rather than a
change of meaning on `discount`. If the meaning must change, the field must be renamed so that
we read `null` and fall over instead of being quietly wrong.

## 2. `fundingNetwork` is a card network name

**Where:** `ReceiptTemplateSelector.templateFor` via `FundingNetwork.parseOrNull`.

**Why we need it:** network-funded promotions carry the funding network's branding
requirements. A Visa-funded discount has to say so on the customer receipt.

**If the field is renamed or its values change:** we fall back to the generic template. Receipts
render, checkout stays up, and we are in breach of the scheme branding agreements for every
affected order. The only signal is `ReceiptTemplateFallbackRate`.

## 3. `status` is `REDEEMED` when the money is in

**Where:** `ReleaseGate.mayRelease`.

**Why we need it:** we release goods only when paid.

**If a new status is introduced:** an unrecognised status reads as "not paid", so those orders
are held. Safe in direction, but fulfilment silently stops for them and the only signal is
`OrdersStuckUnreleased`.

## 4. We cannot see how the card is charged

`coupon-service` charges `billing-service` on our behalf. We do not know which billing endpoint
it uses, whether that endpoint applies billing's pre-charge risk guard, or what amount is sent.

If `coupon-service` changed which billing endpoint it calls, nothing in this repository would
change and nothing here would detect it — but our checkout traffic would be the traffic taking
those charges. This is a dependency we hold and cannot inspect.

## Summary

| Dependency | Failure mode | Customer impact | Detected by |
| --- | --- | --- | --- |
| `discount` is absolute | silent overcharge on every discounted order | wrong price paid | nothing — reconciliation, eventually |
| `fundingNetwork` values | generic receipt, scheme branding breach | cosmetic, contractual | `ReceiptTemplateFallbackRate` |
| `status` closed set | orders held unreleased | delayed parcels | `OrdersStuckUnreleased` |
| billing endpoint choice | unguarded charging on our traffic | fraud exposure | nothing in this repo |
