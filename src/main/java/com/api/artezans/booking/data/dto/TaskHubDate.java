package com.api.artezans.booking.data.dto;

import lombok.*;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskHubDate {

    private int year;

    private int day;

    private int month;
}