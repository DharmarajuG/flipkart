package shop.krishna.product.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import shop.krishna.common.error.ConflictException;
import shop.krishna.product.domain.Category;
import shop.krishna.product.dto.CategoryRequest;
import shop.krishna.product.dto.CategoryResponse;
import shop.krishna.product.repository.CategoryRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll().stream().map(CategoryResponse::from).toList();
    }

    @Transactional
    public CategoryResponse create(CategoryRequest req) {
        if (categoryRepository.existsByName(req.name())) {
            throw new ConflictException("Category already exists: " + req.name());
        }
        Category c = Category.builder().name(req.name()).description(req.description()).build();
        return CategoryResponse.from(categoryRepository.save(c));
    }
}
