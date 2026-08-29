package shop.krishna.cart.web;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import shop.krishna.cart.dto.AddItemRequest;
import shop.krishna.cart.dto.CartResponse;
import shop.krishna.cart.service.CartService;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Per-user shopping cart (Redis-backed)")
public class CartController {

    private final CartService cartService;

    @GetMapping
    public CartResponse getCart(Authentication auth) {
        return CartResponse.from(cartService.getCart(userId(auth)));
    }

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public CartResponse addItem(Authentication auth, @Valid @RequestBody AddItemRequest req) {
        return CartResponse.from(cartService.addItem(userId(auth), req));
    }

    @PutMapping("/items/{productId}")
    public CartResponse updateQuantity(Authentication auth,
                                       @PathVariable Long productId,
                                       @RequestParam @Min(0) int quantity) {
        return CartResponse.from(cartService.updateQuantity(userId(auth), productId, quantity));
    }

    @DeleteMapping("/items/{productId}")
    public CartResponse removeItem(Authentication auth, @PathVariable Long productId) {
        return CartResponse.from(cartService.removeItem(userId(auth), productId));
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clear(Authentication auth) {
        cartService.clear(userId(auth));
    }

    private Long userId(Authentication auth) {
        return Long.valueOf(auth.getName());
    }
}
