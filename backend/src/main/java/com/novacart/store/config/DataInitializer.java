package com.novacart.store.config;

import com.novacart.store.entity.Category;
import com.novacart.store.entity.Listing;
import com.novacart.store.entity.ListingCondition;
import com.novacart.store.entity.ListingStatus;
import com.novacart.store.entity.User;
import com.novacart.store.entity.UserRole;
import com.novacart.store.entity.UserStatus;
import com.novacart.store.repository.CategoryRepository;
import com.novacart.store.repository.ListingRepository;
import com.novacart.store.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Component
@Profile("demo")
@DependsOn("categoryDataInitializer")
public class DataInitializer {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ListingRepository listingRepository;
    private final PasswordEncoder passwordEncoder;
    private final String demoPassword;

    public DataInitializer(
            UserRepository userRepository,
            CategoryRepository categoryRepository,
            ListingRepository listingRepository,
            PasswordEncoder passwordEncoder,
            @Value("${RENOVA_DEMO_PASSWORD}") String demoPassword
    ) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.listingRepository = listingRepository;
        this.passwordEncoder = passwordEncoder;
        Assert.hasText(demoPassword, "RENOVA_DEMO_PASSWORD is required when the demo profile is active.");
        Assert.isTrue(demoPassword.length() >= 12, "RENOVA_DEMO_PASSWORD must contain at least 12 characters.");
        this.demoPassword = demoPassword;
    }

    @PostConstruct
    @Transactional
    public void seed() {
        seedUsers();
        seedListings();
    }

    private void seedUsers() {
        ensureUser("ava@renova.local", "Ava Chen", demoPassword, UserRole.USER, "San Francisco, CA");
        ensureUser("liam@renova.local", "Liam Park", demoPassword, UserRole.USER, "Brooklyn, NY");
        ensureUser("nora@renova.local", "Nora Kapoor", demoPassword, UserRole.USER, "Austin, TX");
        ensureUser("sam@renova.local", "Sam Reyes", demoPassword, UserRole.USER, "Seattle, WA");
        userRepository.findByEmailIgnoreCase("admin@renova.local")
                .ifPresent(user -> {
                    user.setStatus(UserStatus.DEACTIVATED);
                    userRepository.save(user);
                });
    }

    private User ensureUser(String email, String displayName, String password, UserRole role, String location) {
        User user = userRepository.findByEmailIgnoreCase(email).orElseGet(User::new);
        user.setEmail(email);
        user.setDisplayName(displayName);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setLocation(location);
        user.setRole(role);
        user.setStatus(UserStatus.ACTIVE);
        user.setBio("Hi, I'm " + displayName.split(" ")[0] + " on ReNova.");
        user.setCreatedAt(Optional.ofNullable(user.getCreatedAt()).orElseGet(Instant::now));
        return userRepository.save(user);
    }

    private void seedListings() {
        if (listingRepository.count() > 0) return;

        User ava = userRepository.findByEmailIgnoreCase("ava@renova.local").orElseThrow();
        User liam = userRepository.findByEmailIgnoreCase("liam@renova.local").orElseThrow();
        User nora = userRepository.findByEmailIgnoreCase("nora@renova.local").orElseThrow();
        User sam = userRepository.findByEmailIgnoreCase("sam@renova.local").orElseThrow();

        Category electronics = categoryRepository.findBySlug("electronics").orElseThrow();
        Category fashion = categoryRepository.findBySlug("fashion").orElseThrow();
        Category home = categoryRepository.findBySlug("home").orElseThrow();
        Category books = categoryRepository.findBySlug("books").orElseThrow();
        Category sports = categoryRepository.findBySlug("sports").orElseThrow();
        Category collectibles = categoryRepository.findBySlug("collectibles").orElseThrow();

        listing(ava, electronics,
                "Apple iPad Air 5th gen, 64GB, Space Gray",
                "Used for note-taking and reading for about 8 months. No scratches on screen, light wear on edges. Original box, charger, and Apple Pencil 2 included.",
                new BigDecimal("380.00"), new BigDecimal("599.00"),
                ListingCondition.LIKE_NEW, "San Francisco, CA", true, new BigDecimal("8.00"),
                List.of(
                        "https://images.unsplash.com/photo-1561154464-82e9adf32764?w=900",
                        "https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=900"
                ));

        listing(liam, fashion,
                "Carhartt WIP Detroit Jacket, Size M",
                "Bought from the Soho store, worn maybe ten times. Aged hamilton brown duck canvas, blanket lining still warm and clean.",
                new BigDecimal("95.00"), new BigDecimal("178.00"),
                ListingCondition.GOOD, "Brooklyn, NY", true, new BigDecimal("12.00"),
                List.of(
                        "https://images.unsplash.com/photo-1551028719-00167b16eac5?w=900",
                        "https://images.unsplash.com/photo-1591047139829-d91aecb6caea?w=900"
                ));

        listing(nora, home,
                "IKEA STRANDMON wing chair, dark blue",
                "Comfortable reading chair, in great shape. Pet-free, smoke-free home. Local pickup in Austin preferred but I can arrange shipping at cost.",
                new BigDecimal("180.00"), new BigDecimal("349.00"),
                ListingCondition.GOOD, "Austin, TX", true, new BigDecimal("0.00"),
                List.of(
                        "https://images.unsplash.com/photo-1555041469-a586c61ea9bc?w=900",
                        "https://images.unsplash.com/photo-1493663284031-b7e3aefcae8e?w=900"
                ));

        listing(sam, books,
                "Designing Data-Intensive Applications, hardcover",
                "Read it twice during my first staff-engineer year. Has occasional pencil underlines (erasable). Cover in great shape.",
                new BigDecimal("18.00"), new BigDecimal("55.00"),
                ListingCondition.GOOD, "Seattle, WA", false, new BigDecimal("6.00"),
                List.of(
                        "https://images.unsplash.com/photo-1532012197267-da84d127e765?w=900"
                ));

        listing(ava, sports,
                "Specialized Sirrus 2.0 hybrid bike, size S",
                "Two years old, garage kept. Recent tune-up at REI, new chain installed last month. Some paint chips on the chainstay.",
                new BigDecimal("320.00"), new BigDecimal("650.00"),
                ListingCondition.GOOD, "San Francisco, CA", true, new BigDecimal("0.00"),
                List.of(
                        "https://images.unsplash.com/photo-1532298229144-0ec0c57515c7?w=900",
                        "https://images.unsplash.com/photo-1485965120184-e220f721d03e?w=900"
                ));

        listing(liam, collectibles,
                "Lego Star Wars Razor Crest 75292, 100% complete",
                "Built once, then displayed in a glass case. All minifigs and the original instructions are included. Box has a small crease on the corner.",
                new BigDecimal("210.00"), new BigDecimal("299.00"),
                ListingCondition.LIKE_NEW, "Brooklyn, NY", true, new BigDecimal("15.00"),
                List.of(
                        "https://images.unsplash.com/photo-1518155317743-a8ff43ea6a5f?w=900"
                ));

        listing(nora, electronics,
                "Bose QuietComfort 45 over-ear headphones",
                "Battery still holds full charge. Pads are clean, case included. Selling because I upgraded to AirPods Max.",
                new BigDecimal("125.00"), new BigDecimal("279.00"),
                ListingCondition.GOOD, "Austin, TX", true, new BigDecimal("8.00"),
                List.of(
                        "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=900"
                ));

        listing(sam, fashion,
                "Vintage Levi's 501 jeans, W32 L32",
                "Honestly, the best fade I've ever worn in. Some honeycomb behind the knees, no holes, no repairs. Vintage USA red tab.",
                new BigDecimal("65.00"), null,
                ListingCondition.GOOD, "Seattle, WA", true, new BigDecimal("8.00"),
                List.of(
                        "https://images.unsplash.com/photo-1542272604-787c3835535d?w=900"
                ));
    }

    private void listing(User seller, Category category, String title, String description,
                         BigDecimal price, BigDecimal originalPrice, ListingCondition condition,
                         String location, boolean negotiable, BigDecimal shippingFee, List<String> images) {
        Listing l = new Listing();
        l.setSeller(seller);
        l.setCategory(category);
        l.setTitle(title);
        l.setDescription(description);
        l.setPrice(price);
        l.setOriginalPrice(originalPrice);
        l.setCondition(condition);
        l.setLocation(location);
        l.setNegotiable(negotiable);
        l.setShippingFee(shippingFee);
        l.setImageUrls(new ArrayList<>(images));
        l.setStatus(ListingStatus.ACTIVE);
        l.setCreatedAt(Instant.now());
        l.setUpdatedAt(Instant.now());
        listingRepository.save(l);
    }
}
