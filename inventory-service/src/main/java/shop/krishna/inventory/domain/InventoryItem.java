package shop.krishna.inventory.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "inventory_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class InventoryItem {

    /** Mirrors product-service product id. */
    @Id
    @Column(name = "product_id")
    private Long productId;

    @Column(nullable = false, length = 60)
    private String sku;

    @Column(name = "available_qty", nullable = false)
    private int availableQty;

    @Column(name = "reserved_qty", nullable = false)
    private int reservedQty;

    /** Optimistic lock guards concurrent reservations. */
    @Version
    private long version;
}
