package com.jobservice.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// This event is published to Kafka when a job is posted
// Notification service will consume it and send email alerts
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobPostedEvent {
    private String jobId;
    private String jobTitle;
    private String companyName;
    private String location;
    private String jobType;
    private String experienceLevel;
    private String salaryRange;
    private String postedByEmail;
    private String postedByName;
}
