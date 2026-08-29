package shop.krishna.payment.gateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Deterministic-ish mock PSP for demos and local runs. Approves with a configurable
 * probability; a real implementation would call Stripe/Razorpay/etc. Swap the bean
 * (payment.gateway.mode=STRIPE) to plug in a real provider.
 */
@Component
public class MockPaymentGateway implements PaymentGateway {

    @Value("${payment.gateway.approve-probability:0.9}")
    private double approveProbability;

    @Override
    public GatewayResult charge(String orderId, Long userId, BigDecimal amount, String currency) {
        // Amounts ending in .13 always decline — makes failure paths easy to test.
        boolean unlucky = amount.remainder(BigDecimal.ONE).compareTo(new BigDecimal("0.13")) == 0;
        boolean approved = !unlucky && ThreadLocalRandom.current().nextDouble() < approveProbability;
        if (approved) {
            return GatewayResult.approved("psp_" + UUID.randomUUID().toString().replace("-", "").substring(0, 20));
        }
        return GatewayResult.declined(unlucky ? "Card declined (test decline amount)" : "Insufficient funds");
    }
}
