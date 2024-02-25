package com.api.artezans.category.data.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import static jakarta.persistence.CascadeType.PERSIST;
import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Services {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private String serviceName;

    @JsonIgnore
    @ManyToOne(cascade = PERSIST)
    @JoinColumn(name = "category_id")
    private Category category;
}