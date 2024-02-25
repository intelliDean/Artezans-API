package com.api.artezans.listings.data.dtos;

import lombok.*;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LocationFilter {

    private String serviceName;

    private String location;
}
