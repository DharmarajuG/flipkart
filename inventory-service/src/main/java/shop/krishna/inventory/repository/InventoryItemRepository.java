package shop.krishna.inventory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shop.krishna.inventory.domain.InventoryItem;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {
}
