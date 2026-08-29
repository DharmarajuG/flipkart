package shop.krishna.cart.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import shop.krishna.cart.client.ProductClient;
import shop.krishna.cart.domain.Cart;
import shop.krishna.cart.domain.CartItem;
import shop.krishna.cart.dto.AddItemRequest;
import shop.krishna.common.error.BadRequestException;
import shop.krishna.common.error.ResourceNotFoundException;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private static final String KEY_PREFIX = "cart:";

    private final RedisTemplate<String, Cart> redis;
    private final ProductClient productClient;

    @Value("${cart.ttl-days:7}")
    private long ttlDays;

    public Cart getCart(Long userId) {
        return Optional.ofNullable(redis.opsForValue().get(key(userId)))
                .orElseGet(() -> new Cart(userId));
    }

    public Cart addItem(Long userId, AddItemRequest req) {
        // Validate the product and snapshot its current price into the cart.
        ProductClient.ProductView product;
        try {
            product = productClient.getById(req.productId());
        } catch (Exception e) {
            throw ResourceNotFoundException.of("Product", req.productId());
        }
        if (product == null || !product.active()) {
            throw new BadRequestException("Product is not available: " + req.productId());
        }

        Cart cart = getCart(userId);
        cart.getItems().stream()
                .filter(i -> i.getProductId().equals(req.productId()))
                .findFirst()
                .ifPresentOrElse(
                        existing -> existing.setQuantity(existing.getQuantity() + req.quantity()),
                        () -> cart.getItems().add(new CartItem(product.id(), product.sku(),
                                product.name(), product.price(), req.quantity())));
        return save(cart);
    }

    public Cart updateQuantity(Long userId, Long productId, int quantity) {
        Cart cart = getCart(userId);
        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> ResourceNotFoundException.of("Cart item", productId));
        if (quantity <= 0) {
            cart.getItems().remove(item);
        } else {
            item.setQuantity(quantity);
        }
        return save(cart);
    }

    public Cart removeItem(Long userId, Long productId) {
        Cart cart = getCart(userId);
        cart.getItems().removeIf(i -> i.getProductId().equals(productId));
        return save(cart);
    }

    public void clear(Long userId) {
        redis.delete(key(userId));
    }

    private Cart save(Cart cart) {
        redis.opsForValue().set(key(cart.getUserId()), cart, Duration.ofDays(ttlDays));
        return cart;
    }

    private String key(Long userId) {
        return KEY_PREFIX + userId;
    }
}
