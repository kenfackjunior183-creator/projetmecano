package com.mecano.notification_service.service;

import com.mecano.notification_service.dto.ReviewRequest;
import com.mecano.notification_service.entity.Review;
import com.mecano.notification_service.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires - ReviewService")
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepo;

    @InjectMocks
    private ReviewService reviewService;

    // ── Données de test ────────────────────────────────────────────
    private UUID mechanicId;
    private UUID automobilistId;
    private UUID repairRequestId;
    private ReviewRequest reviewRequest;
    private Review savedReview;

    @BeforeEach
    void setUp() {
        mechanicId      = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        automobilistId  = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        repairRequestId = UUID.fromString("550e8400-e29b-41d4-a716-446655440002");

        reviewRequest = new ReviewRequest();
        reviewRequest.setMechanicId(mechanicId);
        reviewRequest.setAutomobilistId(automobilistId);
        reviewRequest.setRepairRequestId(repairRequestId);
        reviewRequest.setRating(4);
        reviewRequest.setComment("Très bon travail, rapide et efficace");

        savedReview = Review.builder()
                .id(UUID.randomUUID())
                .mechanicId(mechanicId)
                .automobilistId(automobilistId)
                .repairRequestId(repairRequestId)
                .rating(4)
                .comment("Très bon travail, rapide et efficace")
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ── createReview ───────────────────────────────────────────────

    @Test
    @DisplayName("createReview : doit créer et retourner un avis avec tous les champs")
    void createReview_shouldSaveAndReturnReview() {
        // GIVEN
        when(reviewRepo.save(any(Review.class))).thenReturn(savedReview);

        // WHEN
        Review result = reviewService.createReview(reviewRequest);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getMechanicId()).isEqualTo(mechanicId);
        assertThat(result.getAutomobilistId()).isEqualTo(automobilistId);
        assertThat(result.getRepairRequestId()).isEqualTo(repairRequestId);
        assertThat(result.getRating()).isEqualTo(4);
        assertThat(result.getComment()).isEqualTo("Très bon travail, rapide et efficace");

        verify(reviewRepo, times(1)).save(any(Review.class));
    }

    @Test
    @DisplayName("createReview : doit appeler reviewRepo.save exactement une fois")
    void createReview_shouldCallSaveOnce() {
        // GIVEN
        when(reviewRepo.save(any(Review.class))).thenReturn(savedReview);

        // WHEN
        reviewService.createReview(reviewRequest);

        // THEN
        verify(reviewRepo, times(1)).save(any(Review.class));
        verifyNoMoreInteractions(reviewRepo);
    }

    @Test
    @DisplayName("createReview : doit fonctionner sans commentaire (champ optionnel)")
    void createReview_withoutComment_shouldSucceed() {
        // GIVEN
        reviewRequest.setComment(null);
        Review reviewSansComment = Review.builder()
                .id(UUID.randomUUID())
                .mechanicId(mechanicId)
                .automobilistId(automobilistId)
                .repairRequestId(repairRequestId)
                .rating(5)
                .comment(null)
                .createdAt(LocalDateTime.now())
                .build();
        when(reviewRepo.save(any(Review.class))).thenReturn(reviewSansComment);

        // WHEN
        Review result = reviewService.createReview(reviewRequest);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getComment()).isNull();
        verify(reviewRepo, times(1)).save(any(Review.class));
    }

    // ── getByMechanicId ────────────────────────────────────────────

    @Test
    @DisplayName("getByMechanicId : doit retourner la liste des avis du mécanicien")
    void getByMechanicId_shouldReturnReviews() {
        // GIVEN
        List<Review> reviews = List.of(savedReview);
        when(reviewRepo.findByMechanicId(mechanicId)).thenReturn(reviews);

        // WHEN
        List<Review> result = reviewService.getByMechanicId(mechanicId);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMechanicId()).isEqualTo(mechanicId);
        verify(reviewRepo, times(1)).findByMechanicId(mechanicId);
    }

    @Test
    @DisplayName("getByMechanicId : doit retourner une liste vide si aucun avis")
    void getByMechanicId_noReviews_shouldReturnEmptyList() {
        // GIVEN
        when(reviewRepo.findByMechanicId(mechanicId)).thenReturn(List.of());

        // WHEN
        List<Review> result = reviewService.getByMechanicId(mechanicId);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(reviewRepo, times(1)).findByMechanicId(mechanicId);
    }

    // ── getByAutomobilistId ────────────────────────────────────────

    @Test
    @DisplayName("getByAutomobilistId : doit retourner les avis de l'automobiliste")
    void getByAutomobilistId_shouldReturnReviews() {
        // GIVEN
        List<Review> reviews = List.of(savedReview);
        when(reviewRepo.findByAutomobilistId(automobilistId)).thenReturn(reviews);

        // WHEN
        List<Review> result = reviewService.getByAutomobilistId(automobilistId);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAutomobilistId()).isEqualTo(automobilistId);
        verify(reviewRepo, times(1)).findByAutomobilistId(automobilistId);
    }

    @Test
    @DisplayName("getByAutomobilistId : doit retourner une liste vide si aucun avis")
    void getByAutomobilistId_noReviews_shouldReturnEmptyList() {
        // GIVEN
        when(reviewRepo.findByAutomobilistId(automobilistId)).thenReturn(List.of());

        // WHEN
        List<Review> result = reviewService.getByAutomobilistId(automobilistId);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
        verify(reviewRepo, times(1)).findByAutomobilistId(automobilistId);
    }

    // ── getByRepairRequestId ───────────────────────────────────────

    @Test
    @DisplayName("getByRepairRequestId : doit retourner l'avis correspondant à la réparation")
    void getByRepairRequestId_shouldReturnReview() {
        // GIVEN
        when(reviewRepo.findByRepairRequestId(repairRequestId))
                .thenReturn(Optional.of(savedReview));

        // WHEN
        Optional<Review> result = reviewService.getByRepairRequestId(repairRequestId);

        // THEN
        assertThat(result).isPresent();
        assertThat(result.get().getRepairRequestId()).isEqualTo(repairRequestId);
        verify(reviewRepo, times(1)).findByRepairRequestId(repairRequestId);
    }

    @Test
    @DisplayName("getByRepairRequestId : doit retourner Optional.empty() si aucun avis trouvé")
    void getByRepairRequestId_notFound_shouldReturnEmpty() {
        // GIVEN
        UUID unknownId = UUID.randomUUID();
        when(reviewRepo.findByRepairRequestId(unknownId)).thenReturn(Optional.empty());

        // WHEN
        Optional<Review> result = reviewService.getByRepairRequestId(unknownId);

        // THEN
        assertThat(result).isEmpty();
        verify(reviewRepo, times(1)).findByRepairRequestId(unknownId);
    }

    // ── Tests de rating ────────────────────────────────────────────

    @Test
    @DisplayName("createReview : doit accepter un rating minimum de 1")
    void createReview_withMinRating_shouldSucceed() {
        // GIVEN
        reviewRequest.setRating(1);
        Review reviewMin = Review.builder()
                .id(UUID.randomUUID())
                .mechanicId(mechanicId)
                .automobilistId(automobilistId)
                .repairRequestId(repairRequestId)
                .rating(1)
                .createdAt(LocalDateTime.now())
                .build();
        when(reviewRepo.save(any(Review.class))).thenReturn(reviewMin);

        // WHEN
        Review result = reviewService.createReview(reviewRequest);

        // THEN
        assertThat(result.getRating()).isEqualTo(1);
    }

    @Test
    @DisplayName("createReview : doit accepter un rating maximum de 5")
    void createReview_withMaxRating_shouldSucceed() {
        // GIVEN
        reviewRequest.setRating(5);
        Review reviewMax = Review.builder()
                .id(UUID.randomUUID())
                .mechanicId(mechanicId)
                .automobilistId(automobilistId)
                .repairRequestId(repairRequestId)
                .rating(5)
                .createdAt(LocalDateTime.now())
                .build();
        when(reviewRepo.save(any(Review.class))).thenReturn(reviewMax);

        // WHEN
        Review result = reviewService.createReview(reviewRequest);

        // THEN
        assertThat(result.getRating()).isEqualTo(5);
    }
}

