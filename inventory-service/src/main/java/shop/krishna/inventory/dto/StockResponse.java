package shop.krishna.inventory.dto;

import shop.krishna.inventory.domain.InventoryItem;

public record StockResponse(Long productId, String sku, int availableQty, int reservedQty) {
    public static StockResponse from(InventoryItem i) {
        return new StockResponse(i.getProductId(), i.getSku(), i.getAvailableQty(), i.getReservedQty());
    }
}
