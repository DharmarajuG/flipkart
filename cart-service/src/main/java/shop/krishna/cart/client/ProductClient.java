package shop.krishna.cart.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/** Load-balanced Feign client resolving product-service via Eureka. */
@FeignClient(name = "product-service", path = "/api/products")
public interface ProductClient {

    @GetMapping("/{id}")
    ProductView getById(@PathVariable Long id);

    /** Minimal projection of the product-service response we care about. */
    record ProductView(Long id, String sku, String name, java.math.BigDecimal price,
                       String currency, boolean active) {
    }
}
