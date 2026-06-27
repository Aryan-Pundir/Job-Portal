package com.jobservice.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class JobKafkaProducer {

    private static final String JOB_POSTED_TOPIC = "job-posted";

    @Autowired
    private KafkaTemplate<String, JobPostedEvent> kafkaTemplate;

    public void publishJobPostedEvent(JobPostedEvent event) {
        log.info("Publishing job-posted event to Kafka: jobId={}, title={}",
                event.getJobId(), event.getJobTitle());

        kafkaTemplate.send(JOB_POSTED_TOPIC, event.getJobId(), event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Job event published successfully: topic={}, partition={}, offset={}",
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    } else {
                        log.error("Failed to publish job event: {}", ex.getMessage());
                    }
                });
    }
}
