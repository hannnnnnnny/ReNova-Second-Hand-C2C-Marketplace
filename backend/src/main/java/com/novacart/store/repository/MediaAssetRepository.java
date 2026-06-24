package com.novacart.store.repository;

import com.novacart.store.entity.Listing;
import com.novacart.store.entity.MediaAsset;
import com.novacart.store.entity.MediaAssetStatus;
import com.novacart.store.entity.User;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MediaAssetRepository extends JpaRepository<MediaAsset, Long> {

    Optional<MediaAsset> findByIdAndOwner(Long id, User owner);

    List<MediaAsset> findByListingOrderByDisplayOrderAsc(Listing listing);

    List<MediaAsset> findTop100ByStatusInAndExpiresAtBeforeOrderByExpiresAtAsc(
            Collection<MediaAssetStatus> statuses,
            Instant expiresBefore
    );
}
