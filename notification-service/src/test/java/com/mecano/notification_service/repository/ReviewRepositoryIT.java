package com.mecano.notification_service.repository;

import com.mecano.notification_service.entity.Review;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ReviewRepositoryIT {

    @Autowired
    private ReviewRepository reviewRepository;

    @Test
    void shouldSaveAndRetrieveReview() {
        UUID repairRequestId = UUID.randomUUID();
        UUID automobilistId = UUID.randomUUID();
        UUID mechanicId = UUID.randomUUID();

        Review review = Review.builder()
                .repairRequestId(repairRequestId)
                .automobilistId(automobilistId)
                .mechanicId(mechanicId)
                .rating(5)
                .comment("Excellent travail, très professionnel")
                .build();

        Review saved = reviewRepository.save(review);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();

        Optional<Review> found = reviewRepository.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getRating()).isEqualTo(5);
    }

    @Test
    void shouldFindReviewsByMechanicId() {
        UUID mechanicId = UUID.randomUUID();

        reviewRepository.save(Review.builder()
                .repairRequestId(UUID.randomUUID())
                .automobilistId(UUID.randomUUID())
                .mechanicId(mechanicId)
                .rating(4)
                .comment("Bon service")
                .build());

        List<Review> reviews = reviewRepository.findByMechanicId(mechanicId);

        assertThat(reviews).hasSize(1);
        assertThat(reviews.get(0).getMechanicId()).isEqualTo(mechanicId);
    }

    @Test
    void shouldFindReviewByRepairRequestId() {
        UUID repairRequestId = UUID.randomUUID();

        reviewRepository.save(Review.builder()
                .repairRequestId(repairRequestId)
                .automobilistId(UUID.randomUUID())
                .mechanicId(UUID.randomUUID())
                .rating(3)
                .comment("Correct")
                .build());

        Optional<Review> found = reviewRepository.findByRepairRequestId(repairRequestId);

        assertThat(found).isPresent();
    }
}
