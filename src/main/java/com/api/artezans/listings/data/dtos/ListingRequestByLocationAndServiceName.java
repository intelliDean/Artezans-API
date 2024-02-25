package com.api.artezans.listings.data.dtos;

import lombok.*;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ListingRequestByLocationAndServiceName {
    private String serviceName;
    private String state;
}
