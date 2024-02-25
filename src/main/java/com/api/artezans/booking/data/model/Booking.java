package com.api.artezans.booking.data.model;

import com.api.artezans.booking.data.model.enums.BookingStage;
import com.api.artezans.booking.data.model.enums.BookingState;
import com.api.artezans.listings.data.models.Listing;
import com.api.artezans.users.models.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.CascadeType.PERSIST;
import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Setter
@Getter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class Booking {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ElementCollection
    private Set<LocalDate> bookDates;

    private LocalTime bookFrom;

    private LocalTime bookTo;

    private BigDecimal totalCost;

    @JsonIgnore
    @ManyToOne
    private User user;

    @JsonIgnore
    @OneToOne(targetEntity = Listing.class, cascade = PERSIST)
    private Listing listing;

    @Enumerated(EnumType.STRING)
    private BookingState bookState;

    private boolean accepted;

    @Enumerated(EnumType.STRING)
    private BookingStage bookingStage;

    @CreatedDate
    private final LocalDateTime bookedAt = LocalDateTime.now();

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @OneToOne(targetEntity = BookingAgreement.class, cascade = ALL)
    private BookingAgreement bookingAgreement;
}
