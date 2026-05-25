package com.novacart.store.dto;

import com.novacart.store.entity.Review;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public final class ReviewDtos {

    private ReviewDtos() {}

    public record CreateReviewRequest(
            @NotNull Long orderId,
            @NotNull @Min(1) @Max(5) Integer rating,
            @Size(max = 1000) String comment
    ) {}

    public record ReviewResponse(
            Long id,
            Long orderId,
            String listingTitle,
            UserDtos.PublicUser reviewer,
            UserDtos.PublicUser reviewee,
            int rating,
            String comment,
            String role,
            Instant createdAt
    ) {
        public static ReviewResponse from(Review r) {
            return new ReviewResponse(
                    r.getId(),
                    r.getOrder().getId(),
                    r.getOrder().getListing().getTitle(),
                    UserDtos.PublicUser.from(r.getReviewer()),
                    UserDtos.PublicUser.from(r.getReviewee()),
                    r.getRating(),
                    r.getComment(),
                    r.getRole().name(),
                    r.getCreatedAt()
            );
        }
    }
}
