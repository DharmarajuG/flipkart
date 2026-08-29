package shop.krishna.common.error;

import java.time.Instant;
import java.util.List;

/**
 * Uniform error body returned by every service. Keeps client integration simple
 * and makes gateway/ELK log correlation consistent.
 */
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<String> details
) {
    public static ApiError of(int status, String error, String message, String path, List<String> details) {
        return new ApiError(Instant.now(), status, error, message, path, details == null ? List.of() : details);
    }
}
