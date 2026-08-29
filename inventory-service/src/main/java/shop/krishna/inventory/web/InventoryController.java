package shop.krishna.inventory.web;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import shop.krishna.inventory.dto.StockAdjustRequest;
import shop.krishna.inventory.dto.StockResponse;
import shop.krishna.inventory.service.InventoryService;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Stock levels (reads public via gateway; writes admin-only)")
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/{productId}")
    public StockResponse get(@PathVariable Long productId) {
        return inventoryService.getStock(productId);
    }

    @PutMapping
    public StockResponse upsert(@Valid @RequestBody StockAdjustRequest req) {
        return inventoryService.upsertStock(req.productId(), req.sku(), req.availableQty());
    }
}
