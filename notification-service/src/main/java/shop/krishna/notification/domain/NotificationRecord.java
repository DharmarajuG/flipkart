package shop.krishna.notification.domain;

import java.time.Instant;

/**
 * Lightweight, immutable record of a dispatched (or attempted) notification.
 * Held in an in-memory ring buffer for observability; this service is
 * intentionally stateless and does not own a database.
 */
public record NotificationRecord(
        String channel,
        String to,
        String subject,
        String body,
        String status,
        String detail,
        Instant createdAt
) {
    public static NotificationRecord sent(String channel, String to, String subject, String body) {
        return new NotificationRecord(channel, to, subject, body, "SENT", null, nowStamp());
    }

    public static NotificationRecord failed(String channel, String to, String subject, String detail) {
        return new NotificationRecord(channel, to, subject, null, "FAILED", detail, nowStamp());
    }

    private static Instant nowStamp() {
        return Instant.now();
    }
}
