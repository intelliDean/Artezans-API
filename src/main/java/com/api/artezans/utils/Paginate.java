package com.api.artezans.utils;

import lombok.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Paginate<T> {

    private Long totalElements;

    private Long totalPages;

    private Long pageNumber;

    private Long pageSize;

    private List<T> content;

    public static <T> Paginate<T> fromPage(Page<T> page) {
        return Paginate.<T>builder()
                .totalElements(page.getTotalElements())
                .totalPages((long) page.getTotalPages())
                .pageNumber((long) page.getNumber())
                .pageSize((long) page.getSize())
                .content(page.getContent())
                .build();
    }
}