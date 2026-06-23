package com.novacart.store.config;

import com.novacart.store.entity.Category;
import com.novacart.store.repository.CategoryRepository;
import jakarta.annotation.PostConstruct;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CategoryDataInitializer {

    private final CategoryRepository categoryRepository;

    public CategoryDataInitializer(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @PostConstruct
    @Transactional
    public void seedCategories() {
        if (categoryRepository.count() > 0) return;

        categoryRepository.saveAll(List.of(
                new Category("Electronics", "electronics", "\uD83D\uDCBB", 1),
                new Category("Fashion", "fashion", "\uD83D\uDC55", 2),
                new Category("Home & Living", "home", "\uD83C\uDFE0", 3),
                new Category("Books & Media", "books", "\uD83D\uDCDA", 4),
                new Category("Sports & Outdoors", "sports", "\u26BD", 5),
                new Category("Toys & Games", "toys", "\uD83C\uDFAE", 6),
                new Category("Beauty", "beauty", "\uD83D\uDC84", 7),
                new Category("Collectibles", "collectibles", "\uD83C\uDFAF", 8),
                new Category("Other", "other", "\uD83D\uDCE6", 99)
        ));
    }
}
