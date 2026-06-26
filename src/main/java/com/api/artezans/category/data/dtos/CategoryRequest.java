package com.api.artezans.category.data.dtos;


import lombok.Builder;

import java.util.List;

@Builder
public record CategoryRequest(

        String categoryName,

        List<ServicesDto> servicesDtos) {
}