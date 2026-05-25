package com.novacart.store.repository;

import com.novacart.store.entity.Favorite;
import com.novacart.store.entity.Listing;
import com.novacart.store.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    Optional<Favorite> findByUserAndListing(User user, Listing listing);

    Page<Favorite> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    List<Favorite> findByListing(Listing listing);

    boolean existsByUserAndListing(User user, Listing listing);

    void deleteByUserAndListing(User user, Listing listing);
}
