package com.api.artezans.category.data.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryName {
    @Id
   @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private String name;
}