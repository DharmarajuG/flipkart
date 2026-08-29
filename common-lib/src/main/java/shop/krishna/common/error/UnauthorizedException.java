package shop.krishna.common.error;

import org.springframework.http.HttpStatus;

/** 401 — authentication failed or credentials are invalid. */
public class UnauthorizedException extends ApiException {
    public UnauthorizedException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}
