package shop.krishna.payment.messaging;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import shop.krishna.common.event.PaymentResultEvent;
import shop.krishna.common.event.Topics;

@Component
@RequiredArgsConstructor
public class PaymentEventProducer {

    private final KafkaTemplate<String, Object> kafka;

    public void publishResult(PaymentResultEvent event) {
        String topic = "COMPLETED".equals(event.status()) ? Topics.PAYMENT_COMPLETED : Topics.PAYMENT_FAILED;
        kafka.send(topic, event.orderId(), event);
    }
}
