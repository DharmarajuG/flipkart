package shop.krishna.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shop.krishna.user.domain.Address;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUserId(Long userId);
}
