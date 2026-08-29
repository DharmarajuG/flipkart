package shop.krishna.notification.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Externalised notification settings, bound from {@code notification.*} in the
 * config-repo (see config-repo/notification-service.yml).
 */
@Component
@ConfigurationProperties(prefix = "notification")
@Getter
@Setter
public class NotificationProperties {

    /** Envelope "From" address applied to every outbound email. */
    private String fromAddress = "no-reply@krishna.shop";

    /** How many recent notifications to keep in memory for the observability endpoint. */
    private int recentBufferSize = 200;
}
