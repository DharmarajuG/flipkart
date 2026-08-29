package shop.krishna.common.error;

import org.springframework.http.HttpStatus;

/**
 * Base type for domain exceptions carrying an HTTP status. Concrete subclasses
 * live in their own files ({@link ResourceNotFoundException}, {@link BadRequestException},
 * {@link ConflictException}, {@link UnauthorizedException}).
 */
public abstract class ApiException extends RuntimeException {
    private final HttpStatus status;

    protected ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
