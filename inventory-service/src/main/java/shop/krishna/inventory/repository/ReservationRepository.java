package shop.krishna.inventory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shop.krishna.inventory.domain.Reservation;
import shop.krishna.inventory.domain.ReservationStatus;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByOrderIdAndStatus(String orderId, ReservationStatus status);
    boolean existsByOrderId(String orderId);
}
