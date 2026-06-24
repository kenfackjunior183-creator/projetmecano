package com.mecano.notification_service.service;

import com.mecano.notification_service.dto.ReviewRequest;
import com.mecano.notification_service.entity.Review;
import com.mecano.notification_service.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepo;

    @Transactional
    public Review createReview(ReviewRequest req) {
        Review review = Review.builder()
                .repairRequestId(req.getRepairRequestId())
                .automobilistId(req.getAutomobilistId())
                .mechanicId(req.getMechanicId())
                .rating(req.getRating())
                .comment(req.getComment())
                .build();
        return reviewRepo.save(review);
    }

    public List<Review> getByMechanicId(UUID mechanicId) {
        return reviewRepo.findByMechanicId(mechanicId);
    }

    public List<Review> getByAutomobilistId(UUID automobilistId) {
        return reviewRepo.findByAutomobilistId(automobilistId);
    }

    public Optional<Review> getByRepairRequestId(UUID repairRequestId) {
        return reviewRepo.findByRepairRequestId(repairRequestId);
    }
}