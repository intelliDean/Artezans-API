package com.api.artezans.review.service;

import com.api.artezans.booking.data.model.Booking;
import com.api.artezans.booking.data.repository.BookingRepository;
import com.api.artezans.exceptions.ArtezanException;
import com.api.artezans.review.data.dto.ReviewRequest;
import com.api.artezans.review.data.model.Review;
import com.api.artezans.review.data.repo.ReviewRepository;
import com.api.artezans.users.models.User;
import com.api.artezans.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

import static com.api.artezans.utils.ApiResponse.apiResponse;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;

    @Override
    public ApiResponse submitReview(ReviewRequest request, User customer) {
        // 1. Verify booking exists
        Booking booking = bookingRepository.findById(request.bookingId())
                .orElseThrow(() -> new ArtezanException("Booking not found"));

        // 2. Verify booking belongs to this customer
        if (!booking.getUser().getId().equals(customer.getId())) {
            throw new ArtezanException("You are not authorized to review this booking");
        }

        // 3. Verify booking hasn't been reviewed already
        if (reviewRepository.existsByBookingId(request.bookingId())) {
            throw new ArtezanException("You have already reviewed this booking");
        }

        // 4. Create and save review
        Review review = Review.builder()
                .bookingId(request.bookingId())
                .providerEmail(request.providerEmail())
                .customerName(customer.getFirstName() + " " + customer.getLastName())
                .rating(request.rating())
                .comment(request.comment())
                .build();

        reviewRepository.save(review);
        return apiResponse("Review submitted successfully");
    }

    @Override
    public List<Review> getReviewsForProvider(String providerEmail) {
        return reviewRepository.findAllByProviderEmailIgnoreCase(providerEmail);
    }

    @Override
    public Review getReviewForBooking(Long bookingId) {
        return reviewRepository.findByBookingId(bookingId).orElse(null);
    }
}
