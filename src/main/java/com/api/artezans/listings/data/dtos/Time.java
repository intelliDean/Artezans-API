package com.api.artezans.listings.data.dtos;

import lombok.*;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Time {

    private int hour;

    private int minute;
}
