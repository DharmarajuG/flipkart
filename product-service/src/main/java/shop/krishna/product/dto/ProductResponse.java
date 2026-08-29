package shop.krishna.product.dto;

import shop.krishna.product.domain.Product;

import java.io.Serializable;
import java.math.BigDecimal;

/** Serializable so it can be stored in the Redis product cache. */
public record ProductResponse(
        Long id,
        String sku,
        String name,
        String description,
        BigDecimal price,
        String currency,
        Long categoryId,
        String imageUrl,
        boolean active
) implements Serializable {
    public static ProductResponse from(Product p) {
        return new ProductResponse(p.getId(), p.getSku(), p.getName(), p.getDescription(),
                p.getPrice(), p.getCurrency(), p.getCategoryId(), p.getImageUrl(), p.isActive());
    }
}
