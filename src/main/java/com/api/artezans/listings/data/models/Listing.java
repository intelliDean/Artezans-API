package com.api.artezans.listings.data.models;

import com.api.artezans.listings.data.enums.AvailableDays;
import com.api.artezans.provider.data.model.ServiceProvider;
import com.api.artezans.users.models.Address;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static jakarta.persistence.CascadeType.*;
import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@SQLRestriction("deleted = false")
@Table(
        name = "listings",
        indexes = {
                @Index(name = "idx_listing_service_name", columnList = "serviceName"),
                @Index(name = "idx_listing_service_category", columnList = "serviceCategory"),
                @Index(name = "idx_listing_available", columnList = "available")
        }
)
public class Listing {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false, unique = true)
    private String businessName;

    @Column(nullable = false)
    private String serviceCategory;

    @Column(nullable = false)
    private String serviceName;

    @Column(columnDefinition = "TEXT")
    private String serviceDescription;

    @DecimalMin(value = "5.00", message = "Minimum pricing is AUD$5.00")
    @Column(nullable = false)
    private BigDecimal pricing;

    @ElementCollection
    @CollectionTable(name = "listing_available_days", joinColumns = @JoinColumn(name = "listing_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "available_day")
    private Set<AvailableDays> availableDays;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "service_provider_id", nullable = false)
    private ServiceProvider serviceProvider;

    private boolean available;
    private LocalTime availableFrom;
    private LocalTime availableTo;

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "address_id")
    private Address address;

    private boolean deleted;
    private String stripeId;

    @ElementCollection
    @CollectionTable(name = "listing_pictures", joinColumns = @JoinColumn(name = "listing_id"))
    @Column(name = "picture_url")
    private List<String> businessPictures;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}