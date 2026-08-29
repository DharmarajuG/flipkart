package shop.krishna.common.error;

import org.springframework.http.HttpStatus;

/** 409 — state conflict (duplicate, insufficient stock, already-processed, etc.). */
public class ConflictException extends ApiException {
    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
