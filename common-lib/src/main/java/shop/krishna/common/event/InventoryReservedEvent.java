package shop.krishna.common.event;

import java.time.Instant;

/** inventory-service → order/payment. Stock successfully reserved for the order. */
public record InventoryReservedEvent(
        String orderId,
        String reservationId,
        Instant reservedAt
) {
}
