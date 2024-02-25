package com.api.artezans.booking.data.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.Set;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequest {

    private Long listingId;

    private Set<LocalDate> bookDates;

    private TaskHubTime bookFrom;

    private TaskHubTime bookTo;
}
