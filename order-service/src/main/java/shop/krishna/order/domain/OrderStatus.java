package shop.krishna.order.domain;

/** Order lifecycle states driven by the choreographed saga. */
public enum OrderStatus {
    PENDING,             // created, awaiting inventory reservation
    INVENTORY_RESERVED,  // stock reserved, awaiting payment
    PAID,                // payment completed
    CONFIRMED,           // fully successful terminal state
    FAILED,              // inventory or payment failed (terminal)
    CANCELLED            // cancelled by user/system (terminal)
}
