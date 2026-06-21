package com.api.artezans.users.dto;

import com.api.artezans.users.models.Address;
import com.api.artezans.users.models.User;
import com.api.artezans.users.models.enums.AccountState;
import com.api.artezans.users.models.enums.Role;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserDTO(
        String stripeId,

        String firstName,

        String lastName,

        String emailAddress,

        String phoneNumber,

        AddressDTO address,

        String profileImage,

        boolean enabled,

        AccountState accountState,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "EEEE, d MMMM, yyyy hh:mm:ssa")
        LocalDate deactivatedAt,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "EEEE, d MMMM, yyyy hh:mm:ssa")
        LocalDateTime registeredAt,

        Set<Role> roles
) {
//    public UserDTO(User user) {
//        this(
//                user.getStripeId(),
//                user.getFirstName(),
//                user.getLastName(),
//                user.getEmailAddress(),
//                user.getPhoneNumber(),
//                user.getAddress() == null ? null : new AddressDTO(user.getAddress()),
//                user.getProfileImage(),
//                user.isEnabled(),
//                user.getAccountState(),
//                user.getDeactivatedAt(),
//                user.getRegisteredAt(),
//                user.getRoles()
//        );
//    }

    public record AddressDTO(
            String streetNumber,
            String streetName,
            String unitNumber,
            String suburb,
            String state,
            String postCode
    ) {
//        public AddressDTO(Address address) {
//            this(
//                    address.getStreetNumber(),
//                    address.getStreetName(),
//                    address.getUnitNumber(),
//                    address.getSuburb(),
//                    address.getState(),
//                    address.getPostCode()
//            );
//        }
    }
}
