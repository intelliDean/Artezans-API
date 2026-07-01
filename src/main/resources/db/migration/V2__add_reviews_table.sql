CREATE TABLE review (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    provider_email VARCHAR(255) NOT NULL,
    customer_name VARCHAR(255) NOT NULL,
    rating INT NOT NULL,
    comment TEXT NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT NOW()
);

-- Indexes for performance
CREATE INDEX idx_review_provider_email ON review(provider_email);
CREATE INDEX idx_review_booking_id ON review(booking_id);
