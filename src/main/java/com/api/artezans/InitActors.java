package com.api.artezans;

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
import org.springframework.beans.factory.annotation.Value;
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
    private final PasswordEncoder passwordEncoder;

   
    @Value("${artezan.admin.email}")
    private String adminEmail;

    @Value("${artezan.admin.password}")
    private String adminPassword;

    @Value("${artezan.customer.email}")
    private String customerEmail;

    @Value("${artezan.customer.password}")
    private String customerPassword;

    @Value("${artezan.provider.email}")
    private String serviceProviderEmail;

    @Value("${artezan.provider.password}")
    private String serviceProviderPassword;


    @PostConstruct
    private void updateAdminPassword() {
        Optional<User> user = userRepository.findUserByEmailAddressIgnoreCase(adminEmail);

        if (user.isPresent()) {
            User gottenUser = user.get();

            if (!passwordEncoder.matches(adminPassword, gottenUser.getPassword())) {
                gottenUser.setPassword(passwordEncoder.encode(adminPassword));
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
        if (!userRepository.existsByEmailAddress(customerEmail)) {
            customerRepository.save(Customer.builder()
                    .user(User.builder()
                            .firstName("Glory")
                            .lastName("Charles")
                            .emailAddress(customerEmail)
                            .address(getAddress())
                            .password(passwordEncoder.encode(customerPassword))
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
                .city("Lekki")
                .state("Lagos")
                .postCode("219003")
                .build();
    }


    @PostConstruct
    private void createServiceProvider() {
        if (!userRepository.existsByEmailAddress(serviceProviderEmail)) {
            serviceProviderRepository.save(
                    ServiceProvider.builder()
                            .user(User.builder()
                                    .firstName("Chiamaka")
                                    .lastName("Osinachi")
                                    .address(getAddress())
                                    .emailAddress(serviceProviderEmail)
                                    .password(passwordEncoder.encode(serviceProviderPassword))
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
