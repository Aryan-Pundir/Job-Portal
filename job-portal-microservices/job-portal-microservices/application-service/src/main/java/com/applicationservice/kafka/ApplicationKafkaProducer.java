package com.applicationservice.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

// Event published when someone applies for a job
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class ApplicationReceivedEvent {
    private String applicationId;
    private String jobId;
    private String jobTitle;
    private String companyName;
    private String applicantName;
    private String applicantEmail;
    private String employerEmail; // to notify employer
    private String status;
}

// Kafka Producer
@Service
@Slf4j
public class ApplicationKafkaProducer {

    private static final String APPLICATION_TOPIC = "application-received";

    @Autowired
    private KafkaTemplate<String, ApplicationReceivedEvent> kafkaTemplate;

    public void publishApplicationEvent(ApplicationReceivedEvent event) {
        log.info("Publishing application-received event: applicant={}, job={}",
                event.getApplicantEmail(), event.getJobTitle());

        kafkaTemplate.send(APPLICATION_TOPIC, event.getApplicationId(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Application event published: offset={}",
                                result.getRecordMetadata().offset());
                    } else {
                        log.error("Failed to publish application event: {}", ex.getMessage());
                    }
                });
    }
}
