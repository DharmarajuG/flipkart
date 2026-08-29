package shop.krishna.payment.web;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import shop.krishna.payment.dto.PaymentResponse;
import shop.krishna.payment.service.PaymentService;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment status lookups (settlement is event-driven)")
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/order/{orderId}")
    public PaymentResponse getByOrder(@PathVariable String orderId) {
        return paymentService.getByOrderId(orderId);
    }
}
