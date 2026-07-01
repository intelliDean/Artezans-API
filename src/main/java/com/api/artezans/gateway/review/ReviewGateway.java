package com.api.artezans.gateway.review;

import com.api.artezans.config.annotation.CurrentUser;
import com.api.artezans.config.security.SecuredUser;
import com.api.artezans.review.data.dto.ReviewRequest;
import com.api.artezans.review.data.model.Review;
import com.api.artezans.review.service.ReviewService;
import com.api.artezans.utils.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@AllArgsConstructor
@Tag(name = "Review Controller")
@RequestMapping("/api/v1/review")
public class ReviewGateway {

    private final ReviewService reviewService;

    @PostMapping("submit")
    @PreAuthorize("hasAuthority('CUSTOMER')")
    @Operation(summary = "Submit review for a booking")
    public ResponseEntity<ApiResponse> submitReview(
            @RequestBody @Valid ReviewRequest request,
            @CurrentUser SecuredUser currentUser
     ) {
        return ResponseEntity.ok(reviewService.submitReview(request, currentUser.getUser()));
    }

    @GetMapping("provider")
    @Operation(summary = "Get reviews for a provider email")
    public ResponseEntity<List<Review>> getReviewsForProvider(@RequestParam String email) {
        return ResponseEntity.ok(reviewService.getReviewsForProvider(email));
    }

    @GetMapping("booking/{bookingId}")
    @Operation(summary = "Get review for a booking id")
    public ResponseEntity<Review> getReviewForBooking(@PathVariable Long bookingId) {
        return ResponseEntity.ok(reviewService.getReviewForBooking(bookingId));
    }
}
