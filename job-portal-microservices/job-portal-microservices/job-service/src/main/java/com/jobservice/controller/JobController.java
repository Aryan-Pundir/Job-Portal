package com.jobservice.controller;

import com.jobservice.dto.JobRequest;
import com.jobservice.model.Job;
import com.jobservice.service.JobService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jobs")
@CrossOrigin(origins = "*")
public class JobController {

    @Autowired private JobService jobService;

    @GetMapping("/all")
    public ResponseEntity<?> getAllJobs() {
        return ResponseEntity.ok(Map.of("success", true, "data", jobService.getAllOpenJobs()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getJobById(@PathVariable String id) {
        return ResponseEntity.ok(Map.of("success", true, "data", jobService.getJobById(id)));
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String jobType,
            @RequestParam(required = false) String experienceLevel) {
        return ResponseEntity.ok(Map.of("success", true,
                "data", jobService.searchJobs(title, location, jobType, experienceLevel)));
    }

    @PostMapping("/post")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYER')")
    public ResponseEntity<?> postJob(@Valid @RequestBody JobRequest request,
                                     HttpServletRequest httpRequest) {
        // Headers injected by API Gateway or passed from frontend
        String employerId = httpRequest.getHeader("X-User-Id");
        String employerName = httpRequest.getHeader("X-User-Name");
        String employerEmail = httpRequest.getHeader("X-User-Email");
        String companyName = httpRequest.getHeader("X-Company-Name");

        Job job = jobService.createJob(request, employerId, employerName, employerEmail, companyName);
        return ResponseEntity.ok(Map.of("success", true, "message", "Job posted", "data", job));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYER')")
    public ResponseEntity<?> updateJob(@PathVariable String id,
                                       @Valid @RequestBody JobRequest request,
                                       HttpServletRequest httpRequest) {
        String employerId = httpRequest.getHeader("X-User-Id");
        return ResponseEntity.ok(Map.of("success", true, "data", jobService.updateJob(id, request, employerId)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYER')")
    public ResponseEntity<?> deleteJob(@PathVariable String id, HttpServletRequest httpRequest) {
        jobService.deleteJob(id, httpRequest.getHeader("X-User-Id"));
        return ResponseEntity.ok(Map.of("success", true, "message", "Job deleted"));
    }

    @PutMapping("/{id}/close")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYER')")
    public ResponseEntity<?> closeJob(@PathVariable String id, HttpServletRequest httpRequest) {
        return ResponseEntity.ok(Map.of("success", true,
                "data", jobService.closeJob(id, httpRequest.getHeader("X-User-Id"))));
    }

    @GetMapping("/my-jobs")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYER')")
    public ResponseEntity<?> myJobs(HttpServletRequest httpRequest) {
        return ResponseEntity.ok(Map.of("success", true,
                "data", jobService.getMyJobs(httpRequest.getHeader("X-User-Id"))));
    }
}
