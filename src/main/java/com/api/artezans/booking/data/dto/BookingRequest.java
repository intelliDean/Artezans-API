package com.api.artezans.booking.data.dto;

import java.time.LocalDate;
import java.util.Set;

public record BookingRequest(

        Long listingId,

        Set<LocalDate> bookDates,

        ArtezanTime bookFrom,

        ArtezanTime bookTo
) {}
