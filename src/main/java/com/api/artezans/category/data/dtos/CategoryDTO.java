package com.api.artezans.category.data.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CategoryDTO(

        String categoryName,

        List<String> services,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "EEEE, d MMMM, yyyy hh:mm:ssa")
        LocalDateTime createdAt,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "EEEE, d MMMM, yyyy hh:mm:ssa")
        LocalDateTime updatedAt
) {}