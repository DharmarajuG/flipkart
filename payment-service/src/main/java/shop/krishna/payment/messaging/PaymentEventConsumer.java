package shop.krishna.payment.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import shop.krishna.common.event.PaymentRequestedEvent;
import shop.krishna.common.event.Topics;
import shop.krishna.payment.service.PaymentService;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

    private final PaymentService paymentService;

    @KafkaListener(topics = Topics.PAYMENT_REQUESTED, groupId = "payment-service")
    public void onPaymentRequested(PaymentRequestedEvent event) {
        log.info("[payment] request received for order {} amount {}", event.orderId(), event.amount());
        paymentService.process(event);
    }
}
