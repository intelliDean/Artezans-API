package com.api.artezans.listings.data.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import task.hub.user.listings.data.enums.AvailableDays;
import task.hub.user.service.provider.data.model.ServiceProvider;
import task.hub.user.users.models.Address;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static jakarta.persistence.CascadeType.*;
import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Setter
@Getter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class Listing {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private String businessName;

    private String serviceCategory;

    private String serviceName;

    @Column(columnDefinition = "TEXT")
    private String serviceDescription;

    private BigDecimal pricing; //minimum AUD$5

    @Enumerated(EnumType.STRING)
    private Set<AvailableDays> availableDays;

    @JsonIgnore
    @ManyToOne(cascade = PERSIST)
    private ServiceProvider serviceProvider;

    private boolean available;

    private LocalTime availableFrom;

    private LocalTime availableTo;

    @OneToOne(cascade = ALL)
    private Address address;

    private boolean deleted;

    private String stripeId;

    @ElementCollection
    private List<String> businessPictures;

    public Listing(String serviceName) {
        this.serviceName = serviceName;

    }

    public Listing(Address address) {
        this.address = address;
    }
}