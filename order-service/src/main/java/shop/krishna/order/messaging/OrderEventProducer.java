package shop.krishna.order.messaging;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import shop.krishna.common.event.OrderCreatedEvent;
import shop.krishna.common.event.PaymentRequestedEvent;
import shop.krishna.common.event.Topics;

@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private final KafkaTemplate<String, Object> kafka;

    /** Fan-out to inventory-service to trigger stock reservation. */
    public void publishOrderCreated(OrderCreatedEvent event) {
        kafka.send(Topics.ORDER_CREATED, event.orderId(), event);
    }

    /** Ask payment-service to settle once stock is reserved. */
    public void publishPaymentRequested(PaymentRequestedEvent event) {
        kafka.send(Topics.PAYMENT_REQUESTED, event.orderId(), event);
    }

    public void publishOrderConfirmed(String orderId, Object payload) {
        kafka.send(Topics.ORDER_CONFIRMED, orderId, payload);
    }

    /** Compensating action: release reserved stock when payment fails. */
    public void publishInventoryReleased(String orderId, Object payload) {
        kafka.send(Topics.INVENTORY_RELEASED, orderId, payload);
    }
}
