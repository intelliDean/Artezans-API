package com.api.artezans.booking.data.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.api.artezans.booking.data.model.Booking;
import com.api.artezans.users.models.User;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findAllByUser(User user);

    List<Booking> findAllByListingServiceProviderUser(User user);

    @Query(value = """
            select book from Booking book
            where book.bookState = "OPEN"
            """)
    List<Booking> findOpenBookings();

     @Query(value = """
            select book from Booking book
            where book.bookingStage = "ACCEPTED"
            """)
    List<Booking> findAcceptedBookings();
}
