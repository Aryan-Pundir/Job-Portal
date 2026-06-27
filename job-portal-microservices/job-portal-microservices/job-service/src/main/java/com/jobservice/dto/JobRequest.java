package com.jobservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class JobRequest {
    @NotBlank private String title;
    @NotBlank private String description;
    @NotBlank private String location;
    private String jobType;
    private String experienceLevel;
    private String salaryRange;
    private List<String> skillsRequired;
    private LocalDateTime deadline;
}
