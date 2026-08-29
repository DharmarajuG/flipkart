package shop.krishna.order.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import shop.krishna.common.event.InventoryRejectedEvent;
import shop.krishna.common.event.InventoryReservedEvent;
import shop.krishna.common.event.PaymentResultEvent;
import shop.krishna.common.event.Topics;
import shop.krishna.order.service.OrderService;

/** Consumes saga events that advance or fail an order. */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final OrderService orderService;

    @KafkaListener(topics = Topics.INVENTORY_RESERVED, groupId = "order-service")
    public void onInventoryReserved(InventoryReservedEvent event) {
        log.info("[order] inventory reserved for {}", event.orderId());
        orderService.onInventoryReserved(event);
    }

    @KafkaListener(topics = Topics.INVENTORY_REJECTED, groupId = "order-service")
    public void onInventoryRejected(InventoryRejectedEvent event) {
        log.info("[order] inventory rejected for {}: {}", event.orderId(), event.reason());
        orderService.onInventoryRejected(event);
    }

    @KafkaListener(topics = Topics.PAYMENT_COMPLETED, groupId = "order-service")
    public void onPaymentCompleted(PaymentResultEvent event) {
        log.info("[order] payment completed for {}", event.orderId());
        orderService.onPaymentCompleted(event);
    }

    @KafkaListener(topics = Topics.PAYMENT_FAILED, groupId = "order-service")
    public void onPaymentFailed(PaymentResultEvent event) {
        log.info("[order] payment failed for {}: {}", event.orderId(), event.failureReason());
        orderService.onPaymentFailed(event);
    }
}
