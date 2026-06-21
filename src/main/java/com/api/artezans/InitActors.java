package com.api.artezans;

import com.api.artezans.booking.data.repository.BookingRepository;
import com.api.artezans.customer.data.model.Customer;
import com.api.artezans.customer.data.repository.CustomerRepository;
import com.api.artezans.exceptions.UserNotFoundException;
import com.api.artezans.provider.data.model.ServiceProvider;
import com.api.artezans.provider.data.repository.ServiceProviderRepository;
import com.api.artezans.users.models.Address;
import com.api.artezans.users.models.User;
import com.api.artezans.users.models.enums.AccountState;
import com.api.artezans.users.models.enums.Role;
import com.api.artezans.users.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InitActors {
    private final UserRepository userRepository;
    private final ServiceProviderRepository serviceProviderRepository;
    private final CustomerRepository customerRepository;
    private final BookingRepository bookingRepository;
    private final PasswordEncoder passwordEncoder;
//    private ServiceProvider serviceProvider;
//    private Address address;


    @PostConstruct
    private void updateAdminPassword() {
        final String adminEmail = "oneblockhq@gmail.com";

        Optional<User> user = userRepository.findUserByEmailAddressIgnoreCase(adminEmail);

        if (user.isPresent()) {

            User gottenUser = user.get();

            if (!passwordEncoder.matches("@Oneblock12345!", gottenUser.getPassword())) {
                gottenUser.setPassword(passwordEncoder.encode("@Oneblock12345!"));
                gottenUser.setAddress(getAddress());
                userRepository.save(gottenUser);
                log.info("<<<>Admin password encoded<>>>");
            }
        } else {
            throw new UserNotFoundException("Admin not found");
        }
    }

    @PostConstruct
    private void createCustomer() {
        final String customerEmail = "glory@gmail.com";
        if (!userRepository.existsByEmailAddress(customerEmail)) {
            customerRepository.save(Customer.builder()
                    .user(User.builder()
                            .firstName("Glory")
                            .lastName("Charles")
                            .emailAddress(customerEmail)
                            .address(getAddress())
                            .password(passwordEncoder.encode("@Glory12345!"))
                            .phoneNumber("08064332523")
                            .enabled(true)
                            .accountState(AccountState.VERIFIED)
                            .roles(Collections.singleton(Role.CUSTOMER))
                            .build())
                    .build()
            );
            log.info("<<<>Customer created<>>>");
        }
    }

    private Address getAddress() {
        return Address.builder()
                .unitNumber("5")
                .streetNumber("12")
                .streetName("Lekki Phase 1")
                .suburb("Lekki")
                .state("Lagos")
                .postCode("219003")
                .build();
    }


    @PostConstruct
    private void createServiceProvider() {
        final String serviceProviderEmail = "chiamaka@gmail.com";
        if (!userRepository.existsByEmailAddress(serviceProviderEmail)) {
            serviceProviderRepository.save(
                    ServiceProvider.builder()
                            .user(User.builder()
                                    .firstName("Chiamaka")
                                    .lastName("Osinachi")
                                    .address(getAddress())
                                    .emailAddress(serviceProviderEmail)
                                    .password(passwordEncoder.encode("@Chiamaka12345!"))
                                    .phoneNumber("+2347165332523")
                                    .enabled(true)
                                    .accountState(AccountState.VERIFIED)
                                    .roles(Collections.singleton(Role.SERVICE_PROVIDER))
                                    .build())
                            .build()
            );
            log.info("<<<>Service Provider created<>>>");
        }
    }

}
