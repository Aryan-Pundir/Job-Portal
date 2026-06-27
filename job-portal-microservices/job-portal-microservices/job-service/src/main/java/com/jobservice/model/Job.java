package com.jobservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "jobs")
public class Job {
    @Id
    private String id;
    private String title;
    private String description;
    private String companyName;
    private String location;
    private String jobType;         // FULL_TIME, PART_TIME, INTERNSHIP, REMOTE
    private String experienceLevel; // FRESHER, MID, SENIOR
    private String salaryRange;
    private List<String> skillsRequired;
    private String status;          // OPEN, CLOSED
    private String postedById;
    private String postedByName;
    private String postedByEmail;   // needed for Kafka notifications
    private LocalDateTime postedAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deadline;
}
