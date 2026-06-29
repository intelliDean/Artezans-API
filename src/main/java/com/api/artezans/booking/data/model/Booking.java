package com.api.artezans.booking.data.model;

import com.api.artezans.booking.data.model.enums.BookingStage;
import com.api.artezans.booking.data.model.enums.BookingState;
import com.api.artezans.listings.data.models.Listing;
import com.api.artezans.users.models.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "bookings",
        indexes = {
                @Index(name = "idx_booking_user",    columnList = "user_id"),
                @Index(name = "idx_booking_listing", columnList = "listing_id"),
                @Index(name = "idx_booking_state",   columnList = "bookState"),
                @Index(name = "idx_booking_stage",   columnList = "bookingStage")
        }
)
public class Booking {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "booking_dates",
            joinColumns = @JoinColumn(name = "booking_id")
    )
    @Column(name = "book_date", nullable = false)
    private Set<LocalDate> bookDates;

    @Column(nullable = false)
    private LocalTime bookFrom;

    @Column(nullable = false)
    private LocalTime bookTo;

    @DecimalMin(value = "0.00", message = "Total cost cannot be negative")
    @Column(nullable = false)
    private BigDecimal totalCost;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false)
    private Listing listing;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingState bookState;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean accepted;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStage bookingStage;

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "booking_agreement_id")
    private BookingAgreement bookingAgreement;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "EEEE, d MMMM, yyyy hh:mm:ssa")
    private LocalDateTime bookedAt;

    @LastModifiedDate
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "EEEE, d MMMM, yyyy hh:mm:ssa")
    private LocalDateTime updatedAt;
}