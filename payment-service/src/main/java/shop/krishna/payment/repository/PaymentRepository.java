package shop.krishna.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shop.krishna.payment.domain.Payment;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, String> {
    Optional<Payment> findByOrderId(String orderId);
    boolean existsByOrderId(String orderId);
}
