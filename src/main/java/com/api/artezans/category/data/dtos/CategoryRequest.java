package com.api.artezans.category.data.dtos;


import lombok.*;

import java.util.List;


public record CategoryRequest(

        String categoryName,

        List<ServicesDto> servicesDtos) {
}