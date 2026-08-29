package shop.krishna.payment.dto;

import shop.krishna.payment.domain.Payment;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(
        String id,
        String orderId,
        Long userId,
        BigDecimal amount,
        String currency,
        String status,
        String failureReason,
        Instant createdAt
) {
    public static PaymentResponse from(Payment p) {
        return new PaymentResponse(p.getId(), p.getOrderId(), p.getUserId(), p.getAmount(),
                p.getCurrency(), p.getStatus().name(), p.getFailureReason(), p.getCreatedAt());
    }
}
