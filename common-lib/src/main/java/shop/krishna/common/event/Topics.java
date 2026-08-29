package shop.krishna.common.event;

/** Canonical Kafka topic names shared across producers and consumers. */
public final class Topics {
    private Topics() {}

    public static final String ORDER_CREATED       = "order.created";
    public static final String ORDER_CONFIRMED     = "order.confirmed";
    public static final String ORDER_CANCELLED     = "order.cancelled";

    public static final String PAYMENT_REQUESTED   = "payment.requested";
    public static final String PAYMENT_COMPLETED   = "payment.completed";
    public static final String PAYMENT_FAILED      = "payment.failed";

    public static final String INVENTORY_RESERVE   = "inventory.reserve";
    public static final String INVENTORY_RESERVED  = "inventory.reserved";
    public static final String INVENTORY_REJECTED  = "inventory.rejected";
    public static final String INVENTORY_RELEASED  = "inventory.released";

    public static final String NOTIFICATION_SEND   = "notification.send";
}
