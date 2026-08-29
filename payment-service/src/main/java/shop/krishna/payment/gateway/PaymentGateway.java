package shop.krishna.payment.gateway;

import java.math.BigDecimal;

/** Abstraction over the payment service provider (PSP). */
public interface PaymentGateway {

    GatewayResult charge(String orderId, Long userId, BigDecimal amount, String currency);

    /** Result of a charge attempt. gatewayRef is the PSP transaction id when approved. */
    record GatewayResult(boolean approved, String gatewayRef, String declineReason) {
        public static GatewayResult approved(String ref) {
            return new GatewayResult(true, ref, null);
        }
        public static GatewayResult declined(String reason) {
            return new GatewayResult(false, null, reason);
        }
    }
}
