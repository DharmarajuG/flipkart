package shop.krishna.common.event;

import java.math.BigDecimal;

/** order-service → payment-service. Requests settlement for a reserved order. */
public record PaymentRequestedEvent(
        String orderId,
        Long userId,
        BigDecimal amount,
        String currency
) {
}
