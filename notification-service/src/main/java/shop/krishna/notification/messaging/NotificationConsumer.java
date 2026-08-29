package shop.krishna.notification.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import shop.krishna.common.event.InventoryRejectedEvent;
import shop.krishna.common.event.NotificationEvent;
import shop.krishna.common.event.OrderConfirmedEvent;
import shop.krishna.common.event.PaymentResultEvent;
import shop.krishna.common.event.Topics;
import shop.krishna.notification.service.NotificationService;

/**
 * Subscribes to the business events that should reach the customer and to the
 * generic {@code NOTIFICATION_SEND} channel used by any service that wants to
 * send an ad-hoc message.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = Topics.ORDER_CONFIRMED, groupId = "notification-service")
    public void onOrderConfirmed(OrderConfirmedEvent event) {
        log.info("Received ORDER_CONFIRMED for order {}", event.orderId());
        notificationService.onOrderConfirmed(event);
    }

    @KafkaListener(topics = Topics.PAYMENT_FAILED, groupId = "notification-service")
    public void onPaymentFailed(PaymentResultEvent event) {
        log.info("Received PAYMENT_FAILED for order {}", event.orderId());
        notificationService.onPaymentFailed(event);
    }

    @KafkaListener(topics = Topics.INVENTORY_REJECTED, groupId = "notification-service")
    public void onInventoryRejected(InventoryRejectedEvent event) {
        log.info("Received INVENTORY_REJECTED for order {}", event.orderId());
        notificationService.onInventoryRejected(event);
    }

    @KafkaListener(topics = Topics.NOTIFICATION_SEND, groupId = "notification-service")
    public void onNotificationRequested(NotificationEvent event) {
        log.info("Received NOTIFICATION_SEND channel={} to={}", event.channel(), event.to());
        notificationService.onNotificationRequested(event);
    }
}
