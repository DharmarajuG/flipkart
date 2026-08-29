package shop.krishna.common.event;

import java.math.BigDecimal;
import java.time.Instant;

/** payment-service → order/notification. Payment settled or failed. */
public record PaymentResultEvent(
        String orderId,
        String paymentId,
        String status,          // COMPLETED | FAILED
        BigDecimal amount,
        String currency,
        String failureReason,   // null when COMPLETED
        Instant processedAt
) {
}
