package com.applicationservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

// Feign Client — calls Job Service REST API to get job details
// This is inter-service communication via HTTP (synchronous)
@FeignClient(name = "job-service", url = "${job.service.url}")
public interface JobServiceClient {

    @GetMapping("/api/jobs/{id}")
    Map<String, Object> getJobById(@PathVariable("id") String jobId);
}
