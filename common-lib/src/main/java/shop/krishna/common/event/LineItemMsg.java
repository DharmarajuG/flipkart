package shop.krishna.common.event;

import java.math.BigDecimal;

/** A single order line item, reused across order/inventory/payment events. */
public record LineItemMsg(Long productId, String sku, int quantity, BigDecimal unitPrice) {
}
