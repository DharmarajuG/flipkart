package shop.krishna.common.event;

import java.util.Map;

/** Generic instruction consumed by notification-service. */
public record NotificationEvent(
        String channel,     // EMAIL | SMS
        String to,
        String template,    // e.g. ORDER_CONFIRMED, PAYMENT_FAILED
        Map<String, String> params
) {
}
