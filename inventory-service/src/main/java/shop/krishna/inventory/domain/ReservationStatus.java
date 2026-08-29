package shop.krishna.inventory.domain;

public enum ReservationStatus {
    RESERVED,   // stock held for a pending order
    COMMITTED,  // order confirmed; stock permanently removed
    RELEASED    // order failed/cancelled; stock returned to available
}
