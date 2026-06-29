package com.api.artezans.task.data.model;

import com.api.artezans.users.models.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "task",
        indexes = {
                @Index(name = "idx_task_service_name", columnList = "taskServiceName"),
                @Index(name = "idx_task_poster", columnList = "poster_id"),
                @Index(name = "idx_task_active", columnList = "isActive")
        }
)
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poster_id", nullable = false)
    @JsonIgnore
    private User poster;

    @Column(nullable = false)
    private String taskServiceName;

    @Column(columnDefinition = "TEXT")
    private String taskDescription;

    @Column(nullable = false)
    private String userAddress;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime postedAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean isActive;

    @DecimalMin(value = "0.00", message = "Budget cannot be negative")
    @Column(nullable = false)
    private BigDecimal customerBudget;

    @Column(length = 500)
    private String taskImageUrl;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "task_dates", joinColumns = @JoinColumn(name = "task_id"))
    @Column(name = "task_date", nullable = false)
    private Set<LocalDate> taskDates;
}