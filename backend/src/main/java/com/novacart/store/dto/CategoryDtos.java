package com.novacart.store.dto;

import com.novacart.store.entity.Category;

public final class CategoryDtos {

    private CategoryDtos() {}

    public record CategoryResponse(
            Long id,
            String name,
            String slug,
            String icon,
            int sortOrder
    ) {
        public static CategoryResponse from(Category c) {
            return new CategoryResponse(c.getId(), c.getName(), c.getSlug(), c.getIcon(), c.getSortOrder());
        }
    }
}
