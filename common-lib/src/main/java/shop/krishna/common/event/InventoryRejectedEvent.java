package shop.krishna.common.event;

import java.util.List;

/** inventory-service → order. Reservation could not be satisfied. */
public record InventoryRejectedEvent(
        String orderId,
        String reason,
        List<Long> outOfStockProductIds
) {
}
