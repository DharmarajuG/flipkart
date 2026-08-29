package shop.krishna.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.krishna.common.error.ResourceNotFoundException;
import shop.krishna.common.event.PaymentRequestedEvent;
import shop.krishna.common.event.PaymentResultEvent;
import shop.krishna.payment.domain.Payment;
import shop.krishna.payment.domain.PaymentStatus;
import shop.krishna.payment.dto.PaymentResponse;
import shop.krishna.payment.gateway.PaymentGateway;
import shop.krishna.payment.messaging.PaymentEventProducer;
import shop.krishna.payment.repository.PaymentRepository;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentGateway gateway;
    private final PaymentEventProducer producer;

    /** Processes a payment request. Idempotent per orderId (unique constraint + guard). */
    @Transactional
    public void process(PaymentRequestedEvent req) {
        if (paymentRepository.existsByOrderId(req.orderId())) {
            log.debug("Payment already processed for order {}", req.orderId());
            return;
        }

        var result = gateway.charge(req.orderId(), req.userId(), req.amount(), req.currency());

        Payment payment = Payment.builder()
                .id(UUID.randomUUID().toString())
                .orderId(req.orderId())
                .userId(req.userId())
                .amount(req.amount())
                .currency(req.currency())
                .status(result.approved() ? PaymentStatus.COMPLETED : PaymentStatus.FAILED)
                .failureReason(result.declineReason())
                .gatewayRef(result.gatewayRef())
                .build();
        paymentRepository.save(payment);

        producer.publishResult(new PaymentResultEvent(
                req.orderId(), payment.getId(), payment.getStatus().name(),
                req.amount(), req.currency(), result.declineReason(), Instant.now()));

        log.info("Payment {} for order {}: {}", payment.getId(), req.orderId(), payment.getStatus());
    }

    @Transactional(readOnly = true)
    public PaymentResponse getByOrderId(String orderId) {
        return paymentRepository.findByOrderId(orderId)
                .map(PaymentResponse::from)
                .orElseThrow(() -> ResourceNotFoundException.of("Payment for order", orderId));
    }
}
