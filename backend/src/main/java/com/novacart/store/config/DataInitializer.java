package com.novacart.store.config;

import com.novacart.store.entity.AdminUser;
import com.novacart.store.entity.Category;
import com.novacart.store.entity.Product;
import com.novacart.store.entity.ProductStatus;
import com.novacart.store.repository.AdminUserRepository;
import com.novacart.store.repository.CategoryRepository;
import com.novacart.store.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
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

            Map<String, Category> categories = seedCategories(categoryRepository);
            seedProducts(productRepository, categories);
        };
    }

    private Map<String, Category> seedCategories(CategoryRepository categoryRepository) {
        return Map.of(
                "women", seedCategory(
                        categoryRepository,
                        "Women",
                        "women",
                        "Tailored separates, dresses, knitwear, and soft layers for everyday wardrobes."
                ),
                "men", seedCategory(
                        categoryRepository,
                        "Men",
                        "men",
                        "Clean shirting, relaxed trousers, outerwear, and refined essentials."
                ),
                "bags", seedCategory(
                        categoryRepository,
                        "Bags",
                        "bags",
                        "Crossbodies, totes, clutches, and weekend carry pieces."
                ),
                "jewelry", seedCategory(
                        categoryRepository,
                        "Jewelry",
                        "jewelry",
                        "Minimal earrings, chains, rings, and polished finishing pieces."
                ),
                "shoes", seedCategory(
                        categoryRepository,
                        "Shoes",
                        "shoes",
                        "Loafers, sandals, boots, sneakers, and day-to-evening footwear."
                ),
                "sportswear", seedCategory(
                        categoryRepository,
                        "Sportswear",
                        "sportswear",
                        "Training layers, recovery pieces, and original sports equipment."
                ),
                "accessories", seedCategory(
                        categoryRepository,
                        "Accessories",
                        "accessories",
                        "Scarves, belts, sunglasses, socks, and small style details."
                ),
                "new-arrivals", seedCategory(
                        categoryRepository,
                        "New Arrivals",
                        "new-arrivals",
                        "Freshly added pieces from the latest NovaCart seasonal edit."
                ),
                "sale", seedCategory(
                        categoryRepository,
                        "Sale",
                        "sale",
                        "Limited markdowns on selected last-season fashion and lifestyle pieces."
                ),
                "seasonal-collection", seedCategory(
                        categoryRepository,
                        "Seasonal Collection",
                        "seasonal-collection",
                        "Campaign-led outfits, occasion pieces, and weather-ready layers."
                )
        );
    }

    private void seedProducts(ProductRepository productRepository, Map<String, Category> categories) {
        List<SeedProduct> products = List.of(
                new SeedProduct("silk-wrap-blouse", "Silk Wrap Blouse", "women", "Aster Row", "A fluid wrap blouse with a soft tie waist and subtle sheen for dinner, work, and weekends.", "118.00", "148.00", 24, 5, "/catalog/women.svg", true, ProductStatus.ACTIVE, List.of("spring-edit", "silk", "workwear")),
                new SeedProduct("linen-wide-leg-trouser", "Linen Wide-Leg Trouser", "women", "Linden Vale", "Airy linen trousers with a pressed front crease and comfortable high-rise waist.", "132.00", null, 31, 6, "/catalog/women.svg", true, ProductStatus.ACTIVE, List.of("linen", "summer", "capsule")),
                new SeedProduct("ribbed-midi-dress", "Ribbed Midi Dress", "women", "Rue Forme", "A close but easy rib-knit dress with a clean neckline and seasonless layering weight.", "96.00", null, 19, 4, "/catalog/women.svg", false, ProductStatus.ACTIVE, List.of("dress", "knit", "new-arrival")),
                new SeedProduct("cropped-denim-jacket", "Cropped Denim Jacket", "women", "Aster Row", "A cropped denim layer with bracelet sleeves, tonal stitching, and a structured fit.", "128.00", null, 16, 4, "/catalog/women.svg", false, ProductStatus.ACTIVE, List.of("denim", "jacket", "weekend")),
                new SeedProduct("tailored-wool-coat", "Tailored Wool Coat", "women", "Meridian Atelier", "A lined wool coat with a clean shoulder, deep pockets, and a softly shaped waist.", "286.00", "340.00", 8, 3, "/catalog/women.svg", true, ProductStatus.ACTIVE, List.of("outerwear", "winter", "tailored")),
                new SeedProduct("satin-camisole", "Satin Camisole", "women", "Solace Field", "A bias-cut camisole designed to sit neatly under blazers or stand alone after hours.", "68.00", null, 37, 8, "/catalog/women.svg", false, ProductStatus.ACTIVE, List.of("evening", "satin", "layering")),
                new SeedProduct("merino-overshirt", "Merino Overshirt", "men", "Harbor Finch", "A warm merino overshirt with matte buttons and enough structure to replace a light jacket.", "154.00", null, 22, 5, "/catalog/men.svg", true, ProductStatus.ACTIVE, List.of("merino", "overshirt", "fall")),
                new SeedProduct("relaxed-twill-trouser", "Relaxed Twill Trouser", "men", "Northline Studio", "Soft twill trousers cut with room through the leg and a clean tailored waistband.", "118.00", null, 34, 7, "/catalog/men.svg", false, ProductStatus.ACTIVE, List.of("twill", "workwear", "capsule")),
                new SeedProduct("cotton-poplin-shirt", "Cotton Poplin Shirt", "men", "Harbor Finch", "A crisp poplin shirt with a relaxed collar and a curved hem for tucked or untucked styling.", "82.00", null, 42, 9, "/catalog/men.svg", true, ProductStatus.ACTIVE, List.of("shirt", "cotton", "workwear")),
                new SeedProduct("brushed-crew-sweater", "Brushed Crew Sweater", "men", "Linden Vale", "A brushed cotton crew with rib trim, a soft hand, and an easy weekday fit.", "94.00", "112.00", 27, 6, "/catalog/men.svg", false, ProductStatus.ACTIVE, List.of("knitwear", "crew", "sale")),
                new SeedProduct("city-bomber-jacket", "City Bomber Jacket", "men", "Meridian Atelier", "A streamlined bomber with a smooth recycled shell and quiet utility pockets.", "176.00", null, 14, 4, "/catalog/men.svg", true, ProductStatus.ACTIVE, List.of("outerwear", "city", "weekend")),
                new SeedProduct("pleated-resort-short", "Pleated Resort Short", "men", "Solace Field", "Tailored resort shorts with a soft pleat, corozo-style button, and breathable finish.", "72.00", null, 29, 6, "/catalog/men.svg", false, ProductStatus.ACTIVE, List.of("summer", "short", "resort")),
                new SeedProduct("crescent-shoulder-bag", "Crescent Shoulder Bag", "bags", "Rue Forme", "A sculptural crescent bag with an adjustable strap and smooth lined interior.", "138.00", null, 18, 4, "/catalog/bags.svg", true, ProductStatus.ACTIVE, List.of("bag", "leather", "bestseller")),
                new SeedProduct("structured-work-tote", "Structured Work Tote", "bags", "Meridian Atelier", "A structured tote sized for a laptop, flats, and the daily pieces that need their own place.", "186.00", "228.00", 11, 3, "/catalog/bags.svg", true, ProductStatus.ACTIVE, List.of("tote", "workwear", "commute")),
                new SeedProduct("nylon-belt-bag", "Nylon Belt Bag", "bags", "Kinetic Loom", "A water-resistant belt bag with a low-profile buckle and two internal slip pockets.", "64.00", null, 38, 8, "/catalog/bags.svg", false, ProductStatus.ACTIVE, List.of("travel", "nylon", "sportswear")),
                new SeedProduct("woven-evening-clutch", "Woven Evening Clutch", "bags", "Solace Field", "A small woven clutch with a soft magnetic closure and room for evening essentials.", "92.00", null, 13, 4, "/catalog/bags.svg", false, ProductStatus.ACTIVE, List.of("evening", "clutch", "occasion")),
                new SeedProduct("weekender-duffel", "Weekender Duffel", "bags", "Northline Studio", "A durable weekender with padded handles, a shoe sleeve, and a clean rectangular shape.", "164.00", null, 17, 4, "/catalog/bags.svg", true, ProductStatus.ACTIVE, List.of("travel", "duffel", "lifestyle")),
                new SeedProduct("compact-crossbody", "Compact Crossbody", "bags", "Aster Row", "A compact crossbody with card slots, a secure top zip, and an easy day-to-night strap.", "88.00", "108.00", 26, 6, "/catalog/bags.svg", false, ProductStatus.ACTIVE, List.of("crossbody", "sale", "daywear")),
                new SeedProduct("sculpted-hoop-earrings", "Sculpted Hoop Earrings", "jewelry", "Vale & Thread", "Lightweight sculpted hoops with a polished curve that catches light without feeling loud.", "54.00", null, 48, 10, "/catalog/jewelry.svg", true, ProductStatus.ACTIVE, List.of("earrings", "gold-tone", "bestseller")),
                new SeedProduct("fine-chain-necklace", "Fine Chain Necklace", "jewelry", "Vale & Thread", "A fine chain necklace designed for solo wear or quiet layering with pendants.", "62.00", null, 44, 10, "/catalog/jewelry.svg", false, ProductStatus.ACTIVE, List.of("necklace", "layering", "minimal")),
                new SeedProduct("brushed-signet-ring", "Brushed Signet Ring", "jewelry", "Meridian Atelier", "A softly brushed signet ring with rounded edges and a low, comfortable profile.", "74.00", null, 30, 7, "/catalog/jewelry.svg", false, ProductStatus.ACTIVE, List.of("ring", "silver-tone", "gift")),
                new SeedProduct("pearl-drop-bracelet", "Pearl Drop Bracelet", "jewelry", "Solace Field", "A delicate bracelet finished with small pearl drops and a slim adjustable chain.", "66.00", "82.00", 21, 5, "/catalog/jewelry.svg", true, ProductStatus.ACTIVE, List.of("bracelet", "pearl", "evening")),
                new SeedProduct("layered-ear-cuff-set", "Layered Ear Cuff Set", "jewelry", "Rue Forme", "A set of two ear cuffs for layered styling without an extra piercing.", "38.00", null, 52, 12, "/catalog/jewelry.svg", false, ProductStatus.ACTIVE, List.of("ear-cuff", "stacking", "accessory")),
                new SeedProduct("enamel-charm-pendant", "Enamel Charm Pendant", "jewelry", "Aster Row", "A small enamel charm pendant on a fine chain with a clean pop of color.", "58.00", null, 33, 8, "/catalog/jewelry.svg", false, ProductStatus.ACTIVE, List.of("pendant", "color", "gift")),
                new SeedProduct("soft-leather-loafer", "Soft Leather Loafer", "shoes", "Harbor Finch", "A soft leather loafer with a flexible sole, almond toe, and polished everyday shape.", "148.00", null, 23, 5, "/catalog/shoes.svg", true, ProductStatus.ACTIVE, List.of("loafer", "leather", "workwear")),
                new SeedProduct("strappy-block-heel", "Strappy Block Heel", "shoes", "Solace Field", "A steady block heel with slim straps and a cushioned footbed for long evenings.", "124.00", null, 18, 4, "/catalog/shoes.svg", false, ProductStatus.ACTIVE, List.of("heel", "evening", "occasion")),
                new SeedProduct("clean-court-sneaker", "Clean Court Sneaker", "shoes", "Northline Studio", "A low court sneaker with tonal panels, a padded collar, and a minimal side profile.", "112.00", null, 40, 8, "/catalog/shoes.svg", true, ProductStatus.ACTIVE, List.of("sneaker", "unisex", "bestseller")),
                new SeedProduct("suede-ankle-boot", "Suede Ankle Boot", "shoes", "Meridian Atelier", "A suede ankle boot with a stacked heel and a slim side zip for clean styling.", "168.00", "210.00", 12, 4, "/catalog/shoes.svg", true, ProductStatus.ACTIVE, List.of("boot", "suede", "fall")),
                new SeedProduct("woven-slide-sandal", "Woven Slide Sandal", "shoes", "Linden Vale", "A woven slide sandal with a squared toe, padded strap, and easy summer shape.", "86.00", null, 35, 8, "/catalog/shoes.svg", false, ProductStatus.ACTIVE, List.of("sandal", "summer", "resort")),
                new SeedProduct("trail-inspired-trainer", "Trail Inspired Trainer", "shoes", "Kinetic Loom", "A mixed-material trainer with a grippy sole and city-ready trail detailing.", "132.00", null, 20, 5, "/catalog/shoes.svg", false, ProductStatus.ACTIVE, List.of("trainer", "sportswear", "weekend")),
                new SeedProduct("flex-running-jacket", "Flex Running Jacket", "sportswear", "Kinetic Loom", "A featherweight running jacket with vented panels, packable hood, and secure pocketing.", "118.00", null, 28, 6, "/catalog/sportswear.svg", true, ProductStatus.ACTIVE, List.of("running", "jacket", "active-weekend")),
                new SeedProduct("studio-bike-short", "Studio Bike Short", "sportswear", "Kinetic Loom", "Supportive bike shorts with a smooth waistband and a pocket sized for a phone.", "52.00", null, 46, 10, "/catalog/sportswear.svg", false, ProductStatus.ACTIVE, List.of("training", "short", "studio")),
                new SeedProduct("seamless-training-top", "Seamless Training Top", "sportswear", "Kinetic Loom", "A seamless training top with breathable texture and a close, move-with-you fit.", "48.00", null, 39, 8, "/catalog/sportswear.svg", false, ProductStatus.ACTIVE, List.of("training", "top", "performance")),
                new SeedProduct("court-tennis-skort", "Court Tennis Skort", "sportswear", "Kinetic Loom", "A pleated tennis skort with built-in shorts and a crisp court-ready line.", "74.00", "92.00", 25, 6, "/catalog/sportswear.svg", true, ProductStatus.ACTIVE, List.of("tennis", "skort", "sport")),
                new SeedProduct("studio-yoga-mat", "Studio Yoga Mat", "sportswear", "Solace Field", "A grippy studio mat with alignment marks and a smooth roll for class or home practice.", "68.00", null, 32, 7, "/catalog/sportswear.svg", false, ProductStatus.ACTIVE, List.of("equipment", "yoga", "studio")),
                new SeedProduct("resistance-band-kit", "Resistance Band Kit", "sportswear", "Kinetic Loom", "Five training bands in progressive strengths with a compact drawstring carry pouch.", "34.00", null, 58, 12, "/catalog/sportswear.svg", false, ProductStatus.ACTIVE, List.of("equipment", "training", "travel")),
                new SeedProduct("silk-square-scarf", "Silk Square Scarf", "accessories", "Aster Row", "A silk square scarf with an original abstract print and softly rolled edges.", "72.00", null, 36, 8, "/catalog/accessories.svg", true, ProductStatus.ACTIVE, List.of("scarf", "silk", "print")),
                new SeedProduct("slim-leather-belt", "Slim Leather Belt", "accessories", "Harbor Finch", "A slim leather belt with a clean metal buckle and polished edge finishing.", "58.00", null, 41, 8, "/catalog/accessories.svg", false, ProductStatus.ACTIVE, List.of("belt", "leather", "workwear")),
                new SeedProduct("acetate-sunglasses", "Acetate Sunglasses", "accessories", "Rue Forme", "Sculpted acetate sunglasses with soft square lenses and a lightweight temple feel.", "96.00", null, 28, 6, "/catalog/accessories.svg", true, ProductStatus.ACTIVE, List.of("sunglasses", "summer", "accessory")),
                new SeedProduct("cashmere-rib-beanie", "Cashmere Rib Beanie", "accessories", "Linden Vale", "A soft ribbed beanie in a warm cashmere blend with a neat folded cuff.", "64.00", "78.00", 24, 6, "/catalog/accessories.svg", false, ProductStatus.ACTIVE, List.of("beanie", "winter", "cashmere")),
                new SeedProduct("soft-rib-sock-set", "Soft Rib Sock Set", "accessories", "Solace Field", "Three pairs of soft rib socks in tonal colors for loafers, sneakers, or boots.", "28.00", null, 64, 14, "/catalog/accessories.svg", false, ProductStatus.ACTIVE, List.of("socks", "set", "everyday")),
                new SeedProduct("jewelry-travel-case", "Jewelry Travel Case", "accessories", "Vale & Thread", "A compact travel case with ring rolls, necklace tabs, and a soft microsuede lining.", "44.00", null, 33, 7, "/catalog/accessories.svg", false, ProductStatus.ACTIVE, List.of("travel", "jewelry", "case")),
                new SeedProduct("spring-belted-trench", "Spring Belted Trench", "new-arrivals", "Meridian Atelier", "A lightweight belted trench with storm flaps, a fluid drape, and a removable tie belt.", "214.00", null, 15, 4, "/catalog/new-arrivals.svg", true, ProductStatus.ACTIVE, List.of("new-arrival", "spring-edit", "outerwear")),
                new SeedProduct("sheer-organza-blouse", "Sheer Organza Blouse", "new-arrivals", "Aster Row", "A sheer organza blouse with a tonal camisole lining and soft volume through the sleeve.", "126.00", null, 18, 4, "/catalog/new-arrivals.svg", false, ProductStatus.ACTIVE, List.of("new-arrival", "evening", "blouse")),
                new SeedProduct("utility-midi-skirt", "Utility Midi Skirt", "new-arrivals", "Northline Studio", "A utility midi skirt with angled pockets, a back vent, and a smooth cotton finish.", "98.00", null, 24, 5, "/catalog/new-arrivals.svg", false, ProductStatus.ACTIVE, List.of("new-arrival", "skirt", "utility")),
                new SeedProduct("canvas-high-top", "Canvas High-Top", "new-arrivals", "Kinetic Loom", "A canvas high-top sneaker with tonal laces, a cushioned insole, and a clean rubber foxing.", "92.00", null, 27, 6, "/catalog/new-arrivals.svg", true, ProductStatus.ACTIVE, List.of("new-arrival", "sneaker", "unisex")),
                new SeedProduct("paneled-track-pant", "Paneled Track Pant", "new-arrivals", "Kinetic Loom", "A paneled track pant with a relaxed leg, adjustable hem, and smooth performance fabric.", "88.00", null, 30, 7, "/catalog/new-arrivals.svg", false, ProductStatus.ACTIVE, List.of("new-arrival", "sportswear", "pant")),
                new SeedProduct("mesh-sling-bag", "Mesh Sling Bag", "new-arrivals", "Rue Forme", "A compact mesh sling bag with a curved strap and secure internal card pocket.", "76.00", null, 21, 5, "/catalog/new-arrivals.svg", false, ProductStatus.ACTIVE, List.of("new-arrival", "bag", "weekend")),
                new SeedProduct("last-season-cotton-tee", "Last-Season Cotton Tee", "sale", "Northline Studio", "A soft cotton tee from a previous color story, cut with a straight body and clean neckband.", "28.00", "44.00", 55, 12, "/catalog/sale.svg", false, ProductStatus.ACTIVE, List.of("sale", "tee", "last-season")),
                new SeedProduct("relaxed-cargo-pant", "Relaxed Cargo Pant", "sale", "Harbor Finch", "Relaxed cargo pants with low-profile side pockets and a washed cotton handle.", "72.00", "118.00", 26, 6, "/catalog/sale.svg", true, ProductStatus.ACTIVE, List.of("sale", "cargo", "last-season")),
                new SeedProduct("quilted-liner-vest", "Quilted Liner Vest", "sale", "Linden Vale", "A quilted liner vest with light padding, deep patch pockets, and a neat snap front.", "84.00", "126.00", 17, 4, "/catalog/sale.svg", false, ProductStatus.ACTIVE, List.of("sale", "outerwear", "vest")),
                new SeedProduct("metallic-ballet-flat", "Metallic Ballet Flat", "sale", "Solace Field", "A metallic ballet flat with a rounded toe, soft binding, and barely-there heel.", "78.00", "112.00", 22, 5, "/catalog/sale.svg", false, ProductStatus.ACTIVE, List.of("sale", "shoe", "evening")),
                new SeedProduct("ribbed-tank-duo", "Ribbed Tank Duo", "sale", "Aster Row", "Two ribbed tanks in complementary tones, shaped for layering under shirting and jackets.", "38.00", "58.00", 44, 9, "/catalog/sale.svg", false, ProductStatus.ACTIVE, List.of("sale", "tank", "bundle")),
                new SeedProduct("travel-pouch-set", "Travel Pouch Set", "sale", "Northline Studio", "Three zip pouches for accessories, toiletries, or tech cords, finished in washable nylon.", "34.00", "52.00", 39, 8, "/catalog/sale.svg", false, ProductStatus.ACTIVE, List.of("sale", "travel", "accessory")),
                new SeedProduct("summer-linen-blazer", "Summer Linen Blazer", "seasonal-collection", "Meridian Atelier", "An unlined linen blazer with a relaxed shoulder and natural texture for warm-weather polish.", "196.00", null, 12, 4, "/catalog/seasonal.svg", true, ProductStatus.ACTIVE, List.of("summer-essentials", "linen", "blazer")),
                new SeedProduct("resort-maxi-skirt", "Resort Maxi Skirt", "seasonal-collection", "Solace Field", "A sweeping maxi skirt with a flat front waistband and soft movement through the hem.", "118.00", null, 18, 4, "/catalog/seasonal.svg", false, ProductStatus.ACTIVE, List.of("summer-essentials", "resort", "skirt")),
                new SeedProduct("evening-satin-wrap", "Evening Satin Wrap", "seasonal-collection", "Solace Field", "A long satin wrap that adds soft coverage over dresses, camisoles, and occasion tailoring.", "86.00", null, 25, 6, "/catalog/seasonal.svg", true, ProductStatus.ACTIVE, List.of("evening-details", "satin", "occasion")),
                new SeedProduct("rain-shell-parka", "Rain Shell Parka", "seasonal-collection", "Kinetic Loom", "A lightweight rain shell with taped seams, a drawcord waist, and an adjustable hood.", "158.00", null, 14, 4, "/catalog/seasonal.svg", false, ProductStatus.ACTIVE, List.of("active-weekend", "rain", "outerwear")),
                new SeedProduct("active-weekend-backpack", "Active Weekend Backpack", "seasonal-collection", "Kinetic Loom", "A streamlined backpack with a ventilated back panel, wet pocket, and laptop sleeve.", "128.00", null, 20, 5, "/catalog/seasonal.svg", true, ProductStatus.ACTIVE, List.of("active-weekend", "bag", "equipment")),
                new SeedProduct("holiday-sequin-top", "Holiday Sequin Top", "seasonal-collection", "Aster Row", "A draft campaign top with small tonal sequins and a soft jersey backing for holiday styling.", "108.00", null, 0, 5, "/catalog/seasonal.svg", false, ProductStatus.DRAFT, List.of("evening-details", "holiday", "draft"))
        );

        products.forEach(product -> seedProduct(productRepository, product, categories.get(product.categorySlug())));
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

    private void seedProduct(ProductRepository productRepository, SeedProduct seed, Category category) {
        productRepository.findBySlug(seed.slug()).ifPresentOrElse(
                product -> applySeedProduct(product, seed, category),
                () -> productRepository.save(new Product(
                        seed.name(),
                        seed.slug(),
                        sku(seed.slug()),
                        seed.brand(),
                        seed.description(),
                        new BigDecimal(seed.price()),
                        seed.compareAtPrice() == null ? null : new BigDecimal(seed.compareAtPrice()),
                        seed.stockQuantity(),
                        seed.lowStockThreshold(),
                        seed.imageUrl(),
                        List.of(seed.imageUrl()),
                        seed.tags(),
                        seed.featured(),
                        seed.status(),
                        seed.status() == ProductStatus.ACTIVE,
                        category
                ))
        );
    }

    private void applySeedProduct(Product product, SeedProduct seed, Category category) {
        product.setName(seed.name());
        product.setSku(sku(seed.slug()));
        product.setBrand(seed.brand());
        product.setDescription(seed.description());
        product.setPrice(new BigDecimal(seed.price()));
        product.setCompareAtPrice(seed.compareAtPrice() == null ? null : new BigDecimal(seed.compareAtPrice()));
        product.setLowStockThreshold(seed.lowStockThreshold());
        product.setImageUrl(seed.imageUrl());
        product.setImageGallery(List.of(seed.imageUrl()));
        product.setTags(seed.tags());
        product.setFeatured(seed.featured());
        product.setStatus(seed.status());
        product.setActive(seed.status() == ProductStatus.ACTIVE);
        product.setCategory(category);
    }

    private String sku(String slug) {
        return "NC-" + slug.toUpperCase().replaceAll("[^A-Z0-9]+", "-").replaceAll("(^-|-$)", "");
    }

    private record SeedProduct(
            String slug,
            String name,
            String categorySlug,
            String brand,
            String description,
            String price,
            String compareAtPrice,
            int stockQuantity,
            int lowStockThreshold,
            String imageUrl,
            boolean featured,
            ProductStatus status,
            List<String> tags
    ) {
    }
}
