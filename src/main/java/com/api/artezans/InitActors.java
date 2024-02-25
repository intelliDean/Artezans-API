package com.api.artezans;

import com.api.artezans.booking.data.repository.BookingRepository;
import com.api.artezans.customer.data.model.Customer;
import com.api.artezans.customer.data.repository.CustomerRepository;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class InitActors {
    private final UserRepository userRepository;
    private final ServiceProviderRepository serviceProviderRepository;
    private final CustomerRepository customerRepository;
    private final BookingRepository bookingRepository;
    private final PasswordEncoder passwordEncoder;
    private ServiceProvider serviceProvider;
    private Address address;


    @PostConstruct
    private void creatAdmin() {
        final String adminEmail = "info@taskhub.com";
        User user = userRepository.findUserByEmailAddressIgnoreCase(adminEmail).get();
        if (!passwordEncoder.matches("12345", user.getPassword())) {
            user.setPassword(passwordEncoder.encode("12345"));
            user.setAddress(getAddress());
            userRepository.save(user);
            log.info("<<<>Admin password changed<>>>");
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
                            .password(passwordEncoder.encode("@Bean1234"))
                            .phoneNumber("+61414332523")
                            .isEnabled(true)
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
                .streetName("Alvin")
                .suburb("Logan")
                .state("Olvan")
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
                                    .password(passwordEncoder.encode("@Bean1234"))
                                    .phoneNumber("+61414332523")
                                    .isEnabled(true)
                                    .accountState(AccountState.VERIFIED)
                                    .roles(Collections.singleton(Role.SERVICE_PROVIDER))
                                    .build())
                            .build()
            );
            log.info("<<<>Service Provider created<>>>");
        }
    }

}
