package shop.krishna.product.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.krishna.common.dto.PageResponse;
import shop.krishna.common.error.ConflictException;
import shop.krishna.common.error.ResourceNotFoundException;
import shop.krishna.product.domain.Product;
import shop.krishna.product.dto.ProductRequest;
import shop.krishna.product.dto.ProductResponse;
import shop.krishna.product.repository.ProductRepository;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    /** Cached by id in Redis; evicted on update/delete. */
    @Cacheable(cacheNames = "products", key = "#id")
    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        return productRepository.findById(id)
                .map(ProductResponse::from)
                .orElseThrow(() -> ResourceNotFoundException.of("Product", id));
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> search(String q, Long categoryId, Pageable pageable) {
        Page<Product> page;
        if (q != null && !q.isBlank()) {
            page = productRepository.findByActiveTrueAndNameContainingIgnoreCase(q, pageable);
        } else if (categoryId != null) {
            page = productRepository.findByActiveTrueAndCategoryId(categoryId, pageable);
        } else {
            page = productRepository.findByActiveTrue(pageable);
        }
        return PageResponse.from(page.map(ProductResponse::from));
    }

    @Transactional
    public ProductResponse create(ProductRequest req) {
        if (productRepository.existsBySku(req.sku())) {
            throw new ConflictException("SKU already exists: " + req.sku());
        }
        Product p = Product.builder()
                .sku(req.sku())
                .name(req.name())
                .description(req.description())
                .price(req.price())
                .currency(req.currency().toUpperCase())
                .categoryId(req.categoryId())
                .imageUrl(req.imageUrl())
                .active(req.active() == null || req.active())
                .build();
        return ProductResponse.from(productRepository.save(p));
    }

    @CacheEvict(cacheNames = "products", key = "#id")
    @Transactional
    public ProductResponse update(Long id, ProductRequest req) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Product", id));
        p.setName(req.name());
        p.setDescription(req.description());
        p.setPrice(req.price());
        p.setCurrency(req.currency().toUpperCase());
        p.setCategoryId(req.categoryId());
        p.setImageUrl(req.imageUrl());
        if (req.active() != null) {
            p.setActive(req.active());
        }
        return ProductResponse.from(productRepository.save(p));
    }

    @CacheEvict(cacheNames = "products", key = "#id")
    @Transactional
    public void delete(Long id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Product", id));
        // Soft-delete: keep history, hide from catalog.
        p.setActive(false);
        productRepository.save(p);
    }
}
