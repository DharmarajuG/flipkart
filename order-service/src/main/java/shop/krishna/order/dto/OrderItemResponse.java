package shop.krishna.order.dto;

import shop.krishna.order.domain.OrderItem;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long productId,
        String sku,
        String name,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal lineTotal
) {
    public static OrderItemResponse from(OrderItem i) {
        return new OrderItemResponse(i.getProductId(), i.getSku(), i.getName(),
                i.getUnitPrice(), i.getQuantity(),
                i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())));
    }
}
