package com.api.artezans.task.data.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long posterId ;

    private String taskServiceName;

    @Column(columnDefinition = "TEXT")
    private String taskDescription;

    private String userAddress;

    private LocalDateTime postedAt;

    private boolean isActive;

    private BigDecimal customerBudget;

    private String taskImage;

    @ElementCollection
    private Set<LocalDate> taskDates;
}