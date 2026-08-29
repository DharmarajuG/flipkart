package shop.krishna.common.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** order-service → inventory/payment. Emitted when an order is placed (PENDING). */
public record OrderCreatedEvent(
        String orderId,
        Long userId,
        List<LineItemMsg> items,
        BigDecimal totalAmount,
        String currency,
        Instant createdAt
) {
}
