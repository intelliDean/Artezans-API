package com.api.artezans.task.data.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TaskRequest {

    private String taskServiceName;

    private String taskDescription;

    private String userAddress;

    private BigDecimal customerBudget;

    private MultipartFile taskImage;

    private Set<LocalDate> taskDate;
}