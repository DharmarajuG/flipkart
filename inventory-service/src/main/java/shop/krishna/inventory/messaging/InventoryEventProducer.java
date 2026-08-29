package shop.krishna.inventory.messaging;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import shop.krishna.common.event.InventoryRejectedEvent;
import shop.krishna.common.event.InventoryReservedEvent;
import shop.krishna.common.event.Topics;

@Component
@RequiredArgsConstructor
public class InventoryEventProducer {

    private final KafkaTemplate<String, Object> kafka;

    public void publishReserved(InventoryReservedEvent event) {
        kafka.send(Topics.INVENTORY_RESERVED, event.orderId(), event);
    }

    public void publishRejected(InventoryRejectedEvent event) {
        kafka.send(Topics.INVENTORY_REJECTED, event.orderId(), event);
    }
}
