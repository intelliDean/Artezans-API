package com.api.artezans.booking.data.repository;

import com.api.artezans.booking.data.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
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
