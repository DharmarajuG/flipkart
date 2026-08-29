package shop.krishna.order.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.krishna.common.dto.PageResponse;
import shop.krishna.common.error.BadRequestException;
import shop.krishna.common.error.ResourceNotFoundException;
import shop.krishna.common.event.*;
import shop.krishna.order.client.CartClient;
import shop.krishna.order.domain.Order;
import shop.krishna.order.domain.OrderItem;
import shop.krishna.order.domain.OrderStatus;
import shop.krishna.order.dto.OrderResponse;
import shop.krishna.order.messaging.OrderEventProducer;
import shop.krishna.order.repository.OrderRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartClient cartClient;
    private final OrderEventProducer producer;

    /** Step 1 of the saga: materialize an order from the cart and kick off reservation. */
    @Transactional
    public OrderResponse placeOrder(Long userId) {
        CartClient.CartView cart = cartClient.getCart();
        if (cart == null || cart.items() == null || cart.items().isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }

        Order order = Order.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .status(OrderStatus.PENDING)
                .totalAmount(cart.total())
                .currency("INR")
                .build();

        cart.items().forEach(ci -> order.addItem(OrderItem.builder()
                .productId(ci.productId())
                .sku(ci.sku())
                .name(ci.name())
                .unitPrice(ci.unitPrice())
                .quantity(ci.quantity())
                .build()));

        orderRepository.save(order);

        // Empty the cart now that it has been converted into an order.
        try {
            cartClient.clear();
        } catch (Exception e) {
            log.warn("Failed to clear cart for user {} after order {}", userId, order.getId(), e);
        }

        List<LineItemMsg> lineItems = order.getItems().stream()
                .map(i -> new LineItemMsg(i.getProductId(), i.getSku(), i.getQuantity(), i.getUnitPrice()))
                .toList();
        producer.publishOrderCreated(new OrderCreatedEvent(order.getId(), userId, lineItems,
                order.getTotalAmount(), order.getCurrency(), Instant.now()));

        log.info("Order {} placed (PENDING) for user {}, total {}", order.getId(), userId, order.getTotalAmount());
        return OrderResponse.from(order);
    }

    /** Saga: inventory reserved → request payment. Idempotent. */
    @Transactional
    public void onInventoryReserved(InventoryReservedEvent event) {
        Order order = load(event.orderId());
        if (order.getStatus() != OrderStatus.PENDING) {
            log.debug("Ignoring inventory-reserved for order {} in state {}", order.getId(), order.getStatus());
            return;
        }
        order.setStatus(OrderStatus.INVENTORY_RESERVED);
        orderRepository.save(order);
        producer.publishPaymentRequested(new PaymentRequestedEvent(
                order.getId(), order.getUserId(), order.getTotalAmount(), order.getCurrency()));
    }

    /** Saga: inventory rejected → terminal FAILED. */
    @Transactional
    public void onInventoryRejected(InventoryRejectedEvent event) {
        Order order = load(event.orderId());
        if (isTerminal(order.getStatus())) return;
        order.setStatus(OrderStatus.FAILED);
        order.setFailureReason("Inventory: " + event.reason());
        orderRepository.save(order);
    }

    /** Saga: payment completed → CONFIRMED. Idempotent. */
    @Transactional
    public void onPaymentCompleted(PaymentResultEvent event) {
        Order order = load(event.orderId());
        if (order.getStatus() != OrderStatus.INVENTORY_RESERVED) {
            log.debug("Ignoring payment-completed for order {} in state {}", order.getId(), order.getStatus());
            return;
        }
        order.setStatus(OrderStatus.CONFIRMED);
        order.setPaymentId(event.paymentId());
        orderRepository.save(order);
        producer.publishOrderConfirmed(order.getId(), new OrderConfirmedEvent(
                order.getId(), order.getUserId(), order.getTotalAmount(), order.getCurrency()));
    }

    /** Saga: payment failed → FAILED + release reserved stock (compensation). */
    @Transactional
    public void onPaymentFailed(PaymentResultEvent event) {
        Order order = load(event.orderId());
        if (isTerminal(order.getStatus())) return;
        order.setStatus(OrderStatus.FAILED);
        order.setPaymentId(event.paymentId());
        order.setFailureReason("Payment: " + event.failureReason());
        orderRepository.save(order);
        // Compensating transaction: tell inventory to release the reservation.
        producer.publishInventoryReleased(order.getId(), event);
    }

    @Transactional(readOnly = true)
    public OrderResponse get(String orderId, Long userId) {
        Order order = orderRepository.findWithItemsById(orderId)
                .orElseThrow(() -> ResourceNotFoundException.of("Order", orderId));
        if (!order.getUserId().equals(userId)) {
            throw ResourceNotFoundException.of("Order", orderId);
        }
        return OrderResponse.from(order);
    }

    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> listForUser(Long userId, Pageable pageable) {
        return PageResponse.from(orderRepository.findByUserId(userId, pageable).map(OrderResponse::from));
    }

    private Order load(String orderId) {
        return orderRepository.findWithItemsById(orderId)
                .orElseThrow(() -> ResourceNotFoundException.of("Order", orderId));
    }

    private boolean isTerminal(OrderStatus status) {
        return status == OrderStatus.CONFIRMED || status == OrderStatus.FAILED || status == OrderStatus.CANCELLED;
    }
}
