package com.applicationservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "applications")
public class Application {
    @Id
    private String id;
    private String jobId;
    private String jobTitle;
    private String companyName;
    private String applicantId;
    private String applicantName;
    private String applicantEmail;
    private String coverLetter;
    private String resumeLink;
    private String status; // PENDING, REVIEWED, SHORTLISTED, REJECTED, HIRED
    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;
}
