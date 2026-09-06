# Order API

## `POST /v1/orders`

Places an order. Storefront-facing.

```json
{
  "orderId": "ord-5001",
  "cardNumber": "4111111111111111",
  "couponCode": "NW-VISA-10"
}
```

`couponCode` is optional. Without it, `coupon-service` is not called and the order is charged
and released by the undiscounted path.

### Response

```json
{
  "orderId": "ord-5001",
  "subtotal": "249.00",
  "discountApplied": "10.00",
  "payableTotal": "239.00",
  "receiptTemplate": "receipt/visa-funded",
  "state": "RELEASED"
}
```

`discountApplied` is echoed straight from `coupon-service`'s `discount` field and is treated as
an absolute amount in the order currency. `payableTotal` is `subtotal - discountApplied`.

## Errors

| Status | When |
| --- | --- |
| `400` | `orderId` or `cardNumber` missing |
| `404` | no such order |
| `502` | `coupon-service` could not redeem the coupon — we do not release an unpaid order |
