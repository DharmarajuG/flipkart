package shop.krishna.product.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank @Size(max = 60) String sku,
        @NotBlank @Size(max = 200) String name,
        @Size(max = 2000) String description,
        @NotNull @DecimalMin("0.0") BigDecimal price,
        @NotBlank @Size(min = 3, max = 3) String currency,
        Long categoryId,
        @Size(max = 500) String imageUrl,
        Boolean active
) {
}
