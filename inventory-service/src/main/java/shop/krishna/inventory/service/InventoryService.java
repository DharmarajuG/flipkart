package shop.krishna.inventory.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.krishna.common.error.ResourceNotFoundException;
import shop.krishna.common.event.InventoryRejectedEvent;
import shop.krishna.common.event.InventoryReservedEvent;
import shop.krishna.common.event.OrderCreatedEvent;
import shop.krishna.inventory.domain.InventoryItem;
import shop.krishna.inventory.domain.Reservation;
import shop.krishna.inventory.domain.ReservationStatus;
import shop.krishna.inventory.dto.StockResponse;
import shop.krishna.inventory.messaging.InventoryEventProducer;
import shop.krishna.inventory.repository.InventoryItemRepository;
import shop.krishna.inventory.repository.ReservationRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryItemRepository inventoryRepo;
    private final ReservationRepository reservationRepo;
    private final InventoryEventProducer producer;

    /**
     * Attempts to reserve stock for every line of the order atomically.
     * Idempotent: a duplicate ORDER_CREATED for an already-reserved order is ignored.
     */
    @Transactional
    public void reserve(OrderCreatedEvent event) {
        if (reservationRepo.existsByOrderId(event.orderId())) {
            log.debug("Order {} already has reservations; skipping", event.orderId());
            return;
        }

        List<Long> insufficient = new ArrayList<>();
        List<InventoryItem> toUpdate = new ArrayList<>();
        List<Reservation> reservations = new ArrayList<>();

        for (var line : event.items()) {
            InventoryItem item = inventoryRepo.findById(line.productId()).orElse(null);
            if (item == null || item.getAvailableQty() < line.quantity()) {
                insufficient.add(line.productId());
                continue;
            }
            item.setAvailableQty(item.getAvailableQty() - line.quantity());
            item.setReservedQty(item.getReservedQty() + line.quantity());
            toUpdate.add(item);
            reservations.add(Reservation.builder()
                    .orderId(event.orderId())
                    .productId(line.productId())
                    .quantity(line.quantity())
                    .status(ReservationStatus.RESERVED)
                    .build());
        }

        if (!insufficient.isEmpty()) {
            // Transaction commits nothing (no saves performed) → publish rejection.
            log.info("Rejecting order {}: insufficient stock for {}", event.orderId(), insufficient);
            producer.publishRejected(new InventoryRejectedEvent(
                    event.orderId(), "Insufficient stock", insufficient));
            return;
        }

        inventoryRepo.saveAll(toUpdate);
        reservationRepo.saveAll(reservations);
        producer.publishReserved(new InventoryReservedEvent(
                event.orderId(), event.orderId(), Instant.now()));
        log.info("Reserved stock for order {}", event.orderId());
    }

    /** Compensation: return reserved stock to available (payment failed / cancelled). */
    @Transactional
    public void release(String orderId) {
        var reservations = reservationRepo.findByOrderIdAndStatus(orderId, ReservationStatus.RESERVED);
        for (Reservation r : reservations) {
            inventoryRepo.findById(r.getProductId()).ifPresent(item -> {
                item.setAvailableQty(item.getAvailableQty() + r.getQuantity());
                item.setReservedQty(Math.max(0, item.getReservedQty() - r.getQuantity()));
            });
            r.setStatus(ReservationStatus.RELEASED);
        }
        log.info("Released {} reservation(s) for order {}", reservations.size(), orderId);
    }

    /** Fulfillment: order confirmed → permanently remove reserved stock. */
    @Transactional
    public void commit(String orderId) {
        var reservations = reservationRepo.findByOrderIdAndStatus(orderId, ReservationStatus.RESERVED);
        for (Reservation r : reservations) {
            inventoryRepo.findById(r.getProductId()).ifPresent(item ->
                    item.setReservedQty(Math.max(0, item.getReservedQty() - r.getQuantity())));
            r.setStatus(ReservationStatus.COMMITTED);
        }
        log.info("Committed {} reservation(s) for order {}", reservations.size(), orderId);
    }

    @Transactional
    public StockResponse upsertStock(Long productId, String sku, int availableQty) {
        InventoryItem item = inventoryRepo.findById(productId)
                .orElseGet(() -> InventoryItem.builder().productId(productId).sku(sku).reservedQty(0).build());
        item.setSku(sku);
        item.setAvailableQty(availableQty);
        return StockResponse.from(inventoryRepo.save(item));
    }

    @Transactional(readOnly = true)
    public StockResponse getStock(Long productId) {
        return inventoryRepo.findById(productId)
                .map(StockResponse::from)
                .orElseThrow(() -> ResourceNotFoundException.of("Inventory", productId));
    }
}
