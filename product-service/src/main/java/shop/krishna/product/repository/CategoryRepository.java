package shop.krishna.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import shop.krishna.product.domain.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByName(String name);
}
