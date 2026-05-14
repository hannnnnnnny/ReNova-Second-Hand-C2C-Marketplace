package com.novacart.store.config;

import com.novacart.store.entity.AdminUser;
import com.novacart.store.entity.Category;
import com.novacart.store.entity.Product;
import com.novacart.store.repository.AdminUserRepository;
import com.novacart.store.repository.CategoryRepository;
import com.novacart.store.repository.ProductRepository;
import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    public static final String DEFAULT_ADMIN_EMAIL = "admin@novacart.local";
    public static final String DEFAULT_ADMIN_PASSWORD = "NovaCartAdmin123!";

    @Bean
    CommandLineRunner seedAdminUser(
            AdminUserRepository adminUserRepository,
            CategoryRepository categoryRepository,
            ProductRepository productRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            if (!adminUserRepository.existsByEmailIgnoreCase(DEFAULT_ADMIN_EMAIL)) {
                AdminUser adminUser = new AdminUser(
                        DEFAULT_ADMIN_EMAIL,
                        passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD),
                        "ADMIN",
                        true
                );
                adminUserRepository.save(adminUser);
            }

            Category homeGoods = seedCategory(
                    categoryRepository,
                    "Home Goods",
                    "home-goods",
                    "Well-made essentials for calm, practical living."
            );
            Category studioSupplies = seedCategory(
                    categoryRepository,
                    "Studio Supplies",
                    "studio-supplies",
                    "Tools and materials for organized creative work."
            );
            Category dailyEssentials = seedCategory(
                    categoryRepository,
                    "Daily Essentials",
                    "daily-essentials",
                    "Reliable items designed for everyday routines."
            );
            Category giftSets = seedCategory(
                    categoryRepository,
                    "Gift Sets",
                    "gift-sets",
                    "Thoughtful bundles for useful, memorable gifting."
            );

            seedProduct(
                    productRepository,
                    "Bamboo Desk Organizer",
                    "bamboo-desk-organizer",
                    "A compact organizer with layered compartments for pens, notes, and small workspace tools.",
                    "39.00",
                    28,
                    "https://images.unsplash.com/photo-1586953208448-b95a79798f07?auto=format&fit=crop&w=1200&q=80",
                    studioSupplies
            );
            seedProduct(
                    productRepository,
                    "Linen Market Tote",
                    "linen-market-tote",
                    "A durable everyday tote with reinforced handles and a clean natural texture.",
                    "32.00",
                    42,
                    "https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=1200&q=80",
                    dailyEssentials
            );
            seedProduct(
                    productRepository,
                    "Ceramic Pouring Pitcher",
                    "ceramic-pouring-pitcher",
                    "A hand-finished pitcher for table service, flowers, or a quiet kitchen shelf.",
                    "48.00",
                    16,
                    "https://images.unsplash.com/photo-1514228742587-6b1558fcca3d?auto=format&fit=crop&w=1200&q=80",
                    homeGoods
            );
            seedProduct(
                    productRepository,
                    "Cotton Kitchen Cloth Set",
                    "cotton-kitchen-cloth-set",
                    "A set of four absorbent cotton cloths with a soft weave and muted color palette.",
                    "24.00",
                    64,
                    "https://images.unsplash.com/photo-1582735689369-4fe89db7114c?auto=format&fit=crop&w=1200&q=80",
                    homeGoods
            );
            seedProduct(
                    productRepository,
                    "Notebook Planning Kit",
                    "notebook-planning-kit",
                    "A practical kit with two lay-flat notebooks, page markers, and a slim archival pen.",
                    "29.00",
                    35,
                    "https://images.unsplash.com/photo-1517842645767-c639042777db?auto=format&fit=crop&w=1200&q=80",
                    studioSupplies
            );
            seedProduct(
                    productRepository,
                    "Morning Ritual Gift Box",
                    "morning-ritual-gift-box",
                    "A curated box with a ceramic cup, cotton cloth, and a small planning notebook.",
                    "72.00",
                    12,
                    "https://images.unsplash.com/photo-1512909006721-3d6018887383?auto=format&fit=crop&w=1200&q=80",
                    giftSets
            );
        };
    }

    private Category seedCategory(
            CategoryRepository categoryRepository,
            String name,
            String slug,
            String description
    ) {
        return categoryRepository.findBySlug(slug)
                .orElseGet(() -> categoryRepository.save(new Category(name, slug, description, true)));
    }

    private void seedProduct(
            ProductRepository productRepository,
            String name,
            String slug,
            String description,
            String price,
            int stockQuantity,
            String imageUrl,
            Category category
    ) {
        if (!productRepository.existsBySlug(slug)) {
            productRepository.save(new Product(
                    name,
                    slug,
                    description,
                    new BigDecimal(price),
                    stockQuantity,
                    imageUrl,
                    true,
                    category
            ));
        }
    }
}
