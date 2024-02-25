package com.api.artezans.users.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.*;

import static jakarta.persistence.GenerationType.IDENTITY;

@Setter
@Getter
@Entity
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class Address {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private String streetNumber;

    private String streetName;

    private String unitNumber;

    private String suburb;

    private String state;

    private String postCode;

    public Address(String state){
        this.state = state;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        if (!unitNumber.isEmpty()) {
            sb.append("Unit ").append(unitNumber).append(", ");
        }
        sb.append(streetNumber).append(" ");
        sb.append(streetName).append(", ");
        sb.append(suburb).append(", ");
        sb.append(state).append(", ");
        sb.append(postCode);
        return sb.toString();
    }
}