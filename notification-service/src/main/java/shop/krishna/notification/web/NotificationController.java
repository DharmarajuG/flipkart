package shop.krishna.notification.web;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import shop.krishna.notification.domain.NotificationRecord;
import shop.krishna.notification.service.NotificationService;

import java.util.List;

/**
 * Read-only observability endpoint. The service is otherwise event-driven and
 * has no request-triggered behaviour. Not exposed through the public gateway
 * routes; intended for internal/ops inspection alongside the Kafka listeners.
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Inspect recently dispatched notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/recent")
    public List<NotificationRecord> recent() {
        return notificationService.recent();
    }
}
