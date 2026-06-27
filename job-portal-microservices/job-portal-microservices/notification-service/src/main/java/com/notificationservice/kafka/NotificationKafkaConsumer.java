package com.notificationservice.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

// ===== Event DTOs =====

@Data
@NoArgsConstructor
@AllArgsConstructor
class JobPostedEvent {
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

@Data
@NoArgsConstructor
@AllArgsConstructor
class ApplicationReceivedEvent {
    private String applicationId;
    private String jobId;
    private String jobTitle;
    private String companyName;
    private String applicantName;
    private String applicantEmail;
    private String employerEmail;
    private String status;
}

// ===== Kafka Consumer =====
@Service
@Slf4j
public class NotificationKafkaConsumer {

    @Autowired
    private JavaMailSender mailSender;

    // Consumes from job-posted topic
    @KafkaListener(topics = "job-posted", groupId = "notification-group",
            containerFactory = "jobPostedKafkaListenerContainerFactory")
    public void consumeJobPostedEvent(JobPostedEvent event) {
        log.info("Received job-posted event: jobId={}, title={}", event.getJobId(), event.getJobTitle());

        // Send confirmation email to employer
        sendEmail(
                event.getPostedByEmail(),
                "Job Posted Successfully - " + event.getJobTitle(),
                buildJobPostedEmail(event)
        );

        log.info("Job posted confirmation email sent to: {}", event.getPostedByEmail());
    }

    // Consumes from application-received topic
    @KafkaListener(topics = "application-received", groupId = "notification-group",
            containerFactory = "applicationKafkaListenerContainerFactory")
    public void consumeApplicationEvent(ApplicationReceivedEvent event) {
        log.info("Received application event: applicant={}, job={}", event.getApplicantEmail(), event.getJobTitle());

        // 1. Notify applicant — application received
        sendEmail(
                event.getApplicantEmail(),
                "Application Received - " + event.getJobTitle(),
                buildApplicantEmail(event)
        );

        // 2. Notify employer — new application
        if (event.getEmployerEmail() != null) {
            sendEmail(
                    event.getEmployerEmail(),
                    "New Application for " + event.getJobTitle(),
                    buildEmployerEmail(event)
            );
        }

        log.info("Application notification emails sent");
    }

    private void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@jobportal.com");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    private String buildJobPostedEmail(JobPostedEvent event) {
        return String.format("""
                Hi %s,
                
                Your job has been posted successfully!
                
                Job Title: %s
                Company: %s
                Location: %s
                Job Type: %s
                Experience Level: %s
                Salary Range: %s
                
                Candidates can now apply for this position.
                
                Best regards,
                Job Portal Team
                """,
                event.getPostedByName(), event.getJobTitle(), event.getCompanyName(),
                event.getLocation(), event.getJobType(), event.getExperienceLevel(),
                event.getSalaryRange());
    }

    private String buildApplicantEmail(ApplicationReceivedEvent event) {
        return String.format("""
                Hi %s,
                
                Your application has been received!
                
                Job Title: %s
                Company: %s
                Status: %s
                
                We'll notify you when there are updates on your application.
                
                Best regards,
                Job Portal Team
                """,
                event.getApplicantName(), event.getJobTitle(),
                event.getCompanyName(), event.getStatus());
    }

    private String buildEmployerEmail(ApplicationReceivedEvent event) {
        return String.format("""
                Hello,
                
                You have received a new application!
                
                Job Title: %s
                Applicant: %s (%s)
                
                Log in to review the application.
                
                Best regards,
                Job Portal Team
                """,
                event.getJobTitle(), event.getApplicantName(), event.getApplicantEmail());
    }
}
