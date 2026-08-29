package shop.krishna.inventory.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import shop.krishna.common.event.OrderConfirmedEvent;
import shop.krishna.common.event.OrderCreatedEvent;
import shop.krishna.common.event.PaymentResultEvent;
import shop.krishna.common.event.Topics;
import shop.krishna.inventory.service.InventoryService;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryEventConsumer {

    private final InventoryService inventoryService;

    @KafkaListener(topics = Topics.ORDER_CREATED, groupId = "inventory-service")
    public void onOrderCreated(OrderCreatedEvent event) {
        log.info("[inventory] reserving for order {}", event.orderId());
        inventoryService.reserve(event);
    }

    @KafkaListener(topics = Topics.ORDER_CONFIRMED, groupId = "inventory-service")
    public void onOrderConfirmed(OrderConfirmedEvent event) {
        log.info("[inventory] committing reservation for order {}", event.orderId());
        inventoryService.commit(event.orderId());
    }

    /** order-service forwards the failed PaymentResultEvent here to release stock. */
    @KafkaListener(topics = Topics.INVENTORY_RELEASED, groupId = "inventory-service")
    public void onRelease(PaymentResultEvent event) {
        log.info("[inventory] releasing reservation for order {}", event.orderId());
        inventoryService.release(event.orderId());
    }
}
