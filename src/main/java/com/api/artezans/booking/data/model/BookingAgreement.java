package com.api.artezans.booking.data.model;

import com.api.artezans.booking.data.model.enums.AgreementStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;


@Entity
@Getter
@AllArgsConstructor
@RequiredArgsConstructor
public class BookingAgreement {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private AgreementStatus agreementStatus;

    @Column(columnDefinition = "Text")
    private String message;

    private LocalDateTime agreementTime;

    //FLUENT INTERFACE DESIGN PATTERN
    public BookingAgreement setId(Long id) {
        this.id = id;
        return this;
    }

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
