package shop.krishna.cart.dto;

import shop.krishna.cart.domain.Cart;
import shop.krishna.cart.domain.CartItem;

import java.math.BigDecimal;
import java.util.List;

public record CartResponse(
        Long userId,
        List<CartItem> items,
        int itemCount,
        BigDecimal total
) {
    public static CartResponse from(Cart cart) {
        return new CartResponse(cart.getUserId(), cart.getItems(), cart.itemCount(), cart.total());
    }
}
