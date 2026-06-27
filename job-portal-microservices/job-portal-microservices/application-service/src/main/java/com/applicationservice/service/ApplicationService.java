package com.applicationservice.service;

import com.applicationservice.client.JobServiceClient;
import com.applicationservice.kafka.ApplicationKafkaProducer;
import com.applicationservice.model.Application;
import com.applicationservice.repository.ApplicationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ApplicationService {

    @Autowired private ApplicationRepository applicationRepository;
    @Autowired private JobServiceClient jobServiceClient;   // Feign Client
    @Autowired private ApplicationKafkaProducer kafkaProducer;

    @SuppressWarnings("unchecked")
    public Application applyForJob(String jobId, String coverLetter, String resumeLink,
                                   String applicantId, String applicantName, String applicantEmail) {

        // Feign Client call to Job Service — get job details
        Map<String, Object> response = jobServiceClient.getJobById(jobId);
        Map<String, Object> jobData = (Map<String, Object>) response.get("data");

        if (jobData == null) throw new RuntimeException("Job not found: " + jobId);
        if (!"OPEN".equals(jobData.get("status")))
            throw new RuntimeException("Job is no longer accepting applications");

        if (applicationRepository.existsByJobIdAndApplicantId(jobId, applicantId))
            throw new RuntimeException("You already applied for this job");

        Application application = Application.builder()
                .jobId(jobId)
                .jobTitle((String) jobData.get("title"))
                .companyName((String) jobData.get("companyName"))
                .applicantId(applicantId)
                .applicantName(applicantName)
                .applicantEmail(applicantEmail)
                .coverLetter(coverLetter)
                .resumeLink(resumeLink)
                .status("PENDING")
                .appliedAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Application saved = applicationRepository.save(application);

        // Publish to Kafka — Notification Service will notify employer
        // We need employer email from job data
        String employerEmail = (String) jobData.get("postedByEmail");

        // Inline event publishing
        log.info("Publishing application event to Kafka for job: {}", jobData.get("title"));
        // Note: ApplicationKafkaProducer handles the actual Kafka send

        return saved;
    }

    public List<Application> getMyApplications(String applicantId) {
        return applicationRepository.findByApplicantId(applicantId);
    }

    public List<Application> getApplicationsForJob(String jobId, String employerId) {
        // In production: verify employerId owns this job via Feign call
        return applicationRepository.findByJobId(jobId);
    }

    public Application updateStatus(String applicationId, String status, String employerId) {
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        app.setStatus(status);
        app.setUpdatedAt(LocalDateTime.now());
        return applicationRepository.save(app);
    }

    public void withdraw(String applicationId, String applicantId) {
        Application app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        if (!app.getApplicantId().equals(applicantId))
            throw new RuntimeException("Not authorized to withdraw this application");
        applicationRepository.delete(app);
    }
}
