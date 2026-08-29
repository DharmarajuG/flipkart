package shop.krishna.common.event;

import java.math.BigDecimal;

/** order-service → notification. Emitted when an order reaches CONFIRMED. */
public record OrderConfirmedEvent(
        String orderId,
        Long userId,
        BigDecimal totalAmount,
        String currency
) {
}
