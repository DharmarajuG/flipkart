package shop.krishna.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import shop.krishna.common.event.InventoryRejectedEvent;
import shop.krishna.common.event.NotificationEvent;
import shop.krishna.common.event.OrderConfirmedEvent;
import shop.krishna.common.event.PaymentResultEvent;
import shop.krishna.notification.config.NotificationProperties;
import shop.krishna.notification.domain.NotificationRecord;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Turns domain/business events into user-facing messages and dispatches them.
 *
 * <p>Recipient resolution: business events (order confirmed, payment failed, …)
 * only carry a {@code userId}. In production the real email would be resolved by
 * calling user-service; here we route to a deterministic dev address, which is
 * captured by MailHog regardless of the actual mailbox. Direct {@link NotificationEvent}s
 * already carry an explicit {@code to} address.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final EmailSender emailSender;
    private final NotificationProperties properties;

    /** Bounded, thread-safe view of the most recent notifications (observability only). */
    private final Deque<NotificationRecord> recent = new ArrayDeque<>();

    public void onOrderConfirmed(OrderConfirmedEvent event) {
        String to = devAddressFor(event.userId());
        String subject = "Your krishna.shop order " + event.orderId() + " is confirmed";
        String body = """
                Good news! Your order %s has been confirmed and is being prepared for shipment.

                Order total: %s %s

                Thank you for shopping with krishna.shop.
                """.formatted(event.orderId(), event.totalAmount(), event.currency());
        dispatch("EMAIL", to, subject, body);
    }

    public void onPaymentFailed(PaymentResultEvent event) {
        String to = devAddressFor(event.orderId());
        String subject = "Payment issue with your krishna.shop order " + event.orderId();
        String reason = event.failureReason() != null ? event.failureReason() : "the payment could not be completed";
        String body = """
                We were unable to process the payment for order %s.

                Reason: %s

                Your order has been cancelled and no amount has been charged.
                Please try placing the order again with a different payment method.
                """.formatted(event.orderId(), reason);
        dispatch("EMAIL", to, subject, body);
    }

    public void onInventoryRejected(InventoryRejectedEvent event) {
        String to = devAddressFor(event.orderId());
        String subject = "Some items in order " + event.orderId() + " are unavailable";
        List<Long> out = event.outOfStockProductIds();
        String body = """
                Unfortunately we could not reserve stock for order %s.

                Reason: %s
                Affected products: %s

                Your order has been cancelled. We apologise for the inconvenience.
                """.formatted(event.orderId(), event.reason(), out == null ? "n/a" : out);
        dispatch("EMAIL", to, subject, body);
    }

    public void onNotificationRequested(NotificationEvent event) {
        String channel = event.channel() != null ? event.channel() : "EMAIL";
        String subject = event.template() != null ? event.template() : "krishna.shop notification";
        String body = "Notification: template=" + event.template() + ", params=" + event.params();
        dispatch(channel, event.to(), subject, body);
    }

    private void dispatch(String channel, String to, String subject, String body) {
        try {
            if ("EMAIL".equalsIgnoreCase(channel)) {
                emailSender.send(to, subject, body);
            } else {
                // SMS/PUSH channels are not wired up yet; log so the event is not silently dropped.
                log.info("Channel {} not implemented; would notify to={} subject='{}'", channel, to, subject);
            }
            record(NotificationRecord.sent(channel, to, subject, body));
        } catch (Exception ex) {
            log.error("Failed to dispatch {} notification to={} subject='{}'", channel, to, subject, ex);
            record(NotificationRecord.failed(channel, to, subject, ex.getMessage()));
        }
    }

    private synchronized void record(NotificationRecord rec) {
        recent.addFirst(rec);
        while (recent.size() > properties.getRecentBufferSize()) {
            recent.removeLast();
        }
    }

    /** Snapshot of recent notifications, newest first. */
    public synchronized List<NotificationRecord> recent() {
        return List.copyOf(recent);
    }

    /**
     * Deterministic dev recipient. Replace with a user-service lookup of the real
     * email address before going to production.
     */
    private String devAddressFor(Object userRef) {
        return "user-" + userRef + "@krishna.shop";
    }
}
