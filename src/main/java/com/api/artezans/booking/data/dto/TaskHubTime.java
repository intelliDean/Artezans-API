package com.api.artezans.booking.data.dto;

import lombok.*;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskHubTime {

    private Integer hour;

    private Integer minute;
}
