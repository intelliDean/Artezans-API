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
import com.api.artezans.listings.data.models.Listing;
import com.api.artezans.listings.data.repositories.ListingRepository;
import com.api.artezans.listings.data.enums.AvailableDays;
import com.api.artezans.task.data.model.Task;
import com.api.artezans.task.data.repo.TaskRepository;
import com.api.artezans.booking.data.model.Booking;
import com.api.artezans.booking.data.repository.BookingRepository;
import com.api.artezans.booking.data.model.enums.BookingStage;
import com.api.artezans.booking.data.model.enums.BookingState;
import com.api.artezans.review.data.model.Review;
import com.api.artezans.review.data.repo.ReviewRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class InitActors {

    private final UserRepository userRepository;
    private final ServiceProviderRepository serviceProviderRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final ListingRepository listingRepository;
    private final TaskRepository taskRepository;
    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;

   
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


    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initAll() {
        updateAdminPassword();
        createCustomer();
        createServiceProvider();
        seedMockData();
    }

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

    private void seedMockData() {
        // Only seed if listings table is empty
        if (listingRepository.count() > 0) {
            log.info("<<<>Database already contains listings, skipping seed mock data<>>>");
            return;
        }

        // Get created users from DB
        ServiceProvider provider = serviceProviderRepository.findServiceProviderByUserEmailAddress(serviceProviderEmail)
                .orElseThrow(() -> new IllegalStateException("Service provider not found for seed"));
        User customerUser = userRepository.findUserByEmailAddressIgnoreCase(customerEmail)
                .orElseThrow(() -> new IllegalStateException("Customer not found for seed"));

        Address seedAddr = getAddress();

        // 1. Create and save listings for Chiamaka
        Listing cleaningListing = Listing.builder()
                .businessName("Chiamaka's Professional Cleaning")
                .serviceCategory("HOME SERVICES")
                .serviceName("Cleaning")
                .serviceDescription("Deep cleaning services for apartments, houses, and office spaces. Fully insured with professional equipments.")
                .pricing(new BigDecimal("55.00"))
                .availableDays(new HashSet<>(Arrays.asList(AvailableDays.MONDAY, AvailableDays.WEDNESDAY, AvailableDays.FRIDAY)))
                .available(true)
                .availableFrom(LocalTime.of(9, 0))
                .availableTo(LocalTime.of(17, 0))
                .serviceProvider(provider)
                .address(seedAddr)
                .deleted(false)
                .createdAt(LocalDateTime.now())
                .build();
        listingRepository.save(cleaningListing);

        Listing gardeningListing = Listing.builder()
                .businessName("Chiamaka's Lawn & Garden Care")
                .serviceCategory("HOME SERVICES")
                .serviceName("Landscaping")
                .serviceDescription("Professional lawn mowing, hedge trimming, weeding, and garden design.")
                .pricing(new BigDecimal("65.00"))
                .availableDays(new HashSet<>(Arrays.asList(AvailableDays.TUESDAY, AvailableDays.THURSDAY, AvailableDays.SATURDAY)))
                .available(true)
                .availableFrom(LocalTime.of(8, 0))
                .availableTo(LocalTime.of(16, 0))
                .serviceProvider(provider)
                .address(seedAddr)
                .deleted(false)
                .createdAt(LocalDateTime.now())
                .build();
        listingRepository.save(gardeningListing);

        Listing handymanListing = Listing.builder()
                .businessName("Chiamaka's Handyman Solutions")
                .serviceCategory("HOME IMPROVEMENT")
                .serviceName("Home Maintenance")
                .serviceDescription("Leaking tap repair, door lock replacements, shelf mounting, and general carpentry maintenance.")
                .pricing(new BigDecimal("70.00"))
                .availableDays(new HashSet<>(Arrays.asList(AvailableDays.MONDAY, AvailableDays.TUESDAY, AvailableDays.WEDNESDAY, AvailableDays.THURSDAY, AvailableDays.FRIDAY)))
                .available(true)
                .availableFrom(LocalTime.of(9, 0))
                .availableTo(LocalTime.of(18, 0))
                .serviceProvider(provider)
                .address(seedAddr)
                .deleted(false)
                .createdAt(LocalDateTime.now())
                .build();
        listingRepository.save(handymanListing);

        log.info("<<<>Service listings seeded successfully<>>>");

        // 2. Create and save active tasks for Glory
        Task task1 = Task.builder()
                .poster(customerUser)
                .taskServiceName("House Cleaning")
                .taskDescription("Need a 3-bedroom, 2-bathroom house cleaned before moving out this weekend. Focus on kitchen and bathrooms.")
                .customerBudget(new BigDecimal("200.00"))
                .userAddress("12 Lekki Phase 1, Lekki, Lagos")
                .taskDates(new HashSet<>(Arrays.asList(LocalDate.now().plusDays(1))))
                .isActive(true)
                .postedAt(LocalDateTime.now())
                .build();
        taskRepository.save(task1);

        Task task2 = Task.builder()
                .poster(customerUser)
                .taskServiceName("Lawn Mowing & Weed Trimming")
                .taskDescription("Backyard lawn is overgrown. Need someone to mow it and trim the edges along the fence.")
                .customerBudget(new BigDecimal("120.00"))
                .userAddress("12 Lekki Phase 1, Lekki, Lagos")
                .taskDates(new HashSet<>(Arrays.asList(LocalDate.now().plusDays(2))))
                .isActive(true)
                .postedAt(LocalDateTime.now())
                .build();
        taskRepository.save(task2);

        log.info("<<<>Customer active tasks seeded successfully<>>>");

        // 3. Create completed booking & review
        Booking completedBooking = Booking.builder()
                .bookFrom(LocalTime.of(9, 0))
                .bookTo(LocalTime.of(12, 0))
                .bookState(BookingState.COMPLETED)
                .bookingStage(BookingStage.COMPLETED)
                .totalCost(new BigDecimal("165.00")) // 3 hours * $55
                .bookDates(new HashSet<>(Arrays.asList(LocalDate.now().minusDays(1))))
                .accepted(true)
                .listing(cleaningListing)
                .user(customerUser)
                .bookedAt(LocalDateTime.now().minusDays(1))
                .build();
        bookingRepository.save(completedBooking);

        Review review = Review.builder()
                .bookingId(completedBooking.getId())
                .rating(5)
                .comment("Absolutely spotless work! Chiamaka arrived right on time and did an incredibly thorough job on the kitchen cabinets. Highly recommended!")
                .providerEmail(serviceProviderEmail)
                .customerName("Glory Charles")
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();
        reviewRepository.save(review);

        log.info("<<<>Completed booking and client review seeded successfully<>>>");
    }
}
