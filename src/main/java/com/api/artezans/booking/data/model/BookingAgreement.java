package com.api.artezans.booking.data.model;

import com.api.artezans.booking.data.model.enums.AgreementStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "booking_agreement",
        indexes = {
                @Index(name = "idx_booking_agreement_status", columnList = "agreementStatus")
        }
)
public class BookingAgreement {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AgreementStatus agreementStatus;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "EEEE, d MMMM, yyyy hh:mm:ssa")
    private LocalDateTime agreementTime;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "EEEE, d MMMM, yyyy hh:mm:ssa")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "EEEE, d MMMM, yyyy hh:mm:ssa")
    private LocalDateTime updatedAt;

    // FLUENT INTERFACE DESIGN PATTERN
    public BookingAgreement setAgreementStatus(AgreementStatus agreementStatus) {
        this.agreementStatus = agreementStatus;
        return this;
    }

    public BookingAgreement setMessage(String message) {
        this.message = message;
        return this;
    }

    public BookingAgreement setAgreementTime(LocalDateTime localDateTime) {
        this.agreementTime = localDateTime;
        return this;
    }
}