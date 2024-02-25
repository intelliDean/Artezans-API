package com.api.artezans.category.data.dtos;


import lombok.*;

import java.util.List;

@Setter
@Getter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class CategoryRequest {

    private String categoryName;

    private List<ServicesDto> servicesDtos;
}