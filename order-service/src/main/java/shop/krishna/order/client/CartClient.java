package shop.krishna.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.util.List;

/** Reads and clears the user's cart. Auth headers are forwarded by {@link FeignAuthForwardingConfig}. */
@FeignClient(name = "cart-service", path = "/api/cart", configuration = FeignAuthForwardingConfig.class)
public interface CartClient {

    @GetMapping
    CartView getCart();

    @DeleteMapping
    void clear();

    record CartItemView(Long productId, String sku, String name, BigDecimal unitPrice, int quantity) {}

    record CartView(Long userId, List<CartItemView> items, int itemCount, BigDecimal total) {}
}
