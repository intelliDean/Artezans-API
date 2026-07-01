package com.api.artezans.review.service;

import com.api.artezans.review.data.dto.ReviewRequest;
import com.api.artezans.review.data.model.Review;
import com.api.artezans.users.models.User;
import com.api.artezans.utils.ApiResponse;
import java.util.List;

public interface ReviewService {
    ApiResponse submitReview(ReviewRequest request, User customer);
    List<Review> getReviewsForProvider(String providerEmail);
    Review getReviewForBooking(Long bookingId);
}
