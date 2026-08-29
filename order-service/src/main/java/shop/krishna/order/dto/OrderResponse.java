package shop.krishna.order.dto;

import shop.krishna.order.domain.Order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        String id,
        Long userId,
        String status,
        BigDecimal totalAmount,
        String currency,
        String paymentId,
        String failureReason,
        List<OrderItemResponse> items,
        Instant createdAt
) {
    public static OrderResponse from(Order o) {
        return new OrderResponse(o.getId(), o.getUserId(), o.getStatus().name(),
                o.getTotalAmount(), o.getCurrency(), o.getPaymentId(), o.getFailureReason(),
                o.getItems().stream().map(OrderItemResponse::from).toList(), o.getCreatedAt());
    }
}
