package com.api.artezans.task.data.dto;

import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

public record TaskRequest(

        String taskServiceName,

        String taskDescription,

        String userAddress,

        BigDecimal customerBudget,

        MultipartFile taskImage,

        Set<LocalDate> taskDate) {
}