package shop.krishna.order.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import shop.krishna.common.dto.PageResponse;
import shop.krishna.order.dto.OrderResponse;
import shop.krishna.order.service.OrderService;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Place and track orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Place an order from the current cart (starts the saga)")
    public ResponseEntity<OrderResponse> place(Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.placeOrder(userId(auth)));
    }

    @GetMapping("/{orderId}")
    public OrderResponse get(Authentication auth, @PathVariable String orderId) {
        return orderService.get(orderId, userId(auth));
    }

    @GetMapping
    public PageResponse<OrderResponse> list(Authentication auth,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("createdAt").descending());
        return orderService.listForUser(userId(auth), pageable);
    }

    private Long userId(Authentication auth) {
        return Long.valueOf(auth.getName());
    }
}
