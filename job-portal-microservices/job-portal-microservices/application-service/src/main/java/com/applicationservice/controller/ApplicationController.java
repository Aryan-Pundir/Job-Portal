package com.applicationservice.controller;

import com.applicationservice.model.Application;
import com.applicationservice.service.ApplicationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/applications")
@CrossOrigin(origins = "*")
public class ApplicationController {

    @Autowired private ApplicationService applicationService;

    @PostMapping("/apply/{jobId}")
    @PreAuthorize("hasAuthority('ROLE_JOBSEEKER')")
    public ResponseEntity<?> apply(@PathVariable String jobId,
                                   @RequestBody Map<String, String> body,
                                   HttpServletRequest request) {
        String applicantId = request.getHeader("X-User-Id");
        String applicantName = request.getHeader("X-User-Name");
        String applicantEmail = request.getHeader("X-User-Email");

        Application app = applicationService.applyForJob(
                jobId,
                body.get("coverLetter"),
                body.get("resumeLink"),
                applicantId, applicantName, applicantEmail
        );
        return ResponseEntity.ok(Map.of("success", true, "message", "Applied successfully", "data", app));
    }

    @GetMapping("/my-applications")
    @PreAuthorize("hasAuthority('ROLE_JOBSEEKER')")
    public ResponseEntity<?> myApplications(HttpServletRequest request) {
        return ResponseEntity.ok(Map.of("success", true,
                "data", applicationService.getMyApplications(request.getHeader("X-User-Id"))));
    }

    @DeleteMapping("/{id}/withdraw")
    @PreAuthorize("hasAuthority('ROLE_JOBSEEKER')")
    public ResponseEntity<?> withdraw(@PathVariable String id, HttpServletRequest request) {
        applicationService.withdraw(id, request.getHeader("X-User-Id"));
        return ResponseEntity.ok(Map.of("success", true, "message", "Application withdrawn"));
    }

    @GetMapping("/job/{jobId}")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYER')")
    public ResponseEntity<?> getApplicationsForJob(@PathVariable String jobId,
                                                    HttpServletRequest request) {
        return ResponseEntity.ok(Map.of("success", true,
                "data", applicationService.getApplicationsForJob(jobId, request.getHeader("X-User-Id"))));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYER')")
    public ResponseEntity<?> updateStatus(@PathVariable String id,
                                          @RequestParam String status,
                                          HttpServletRequest request) {
        Application app = applicationService.updateStatus(id, status, request.getHeader("X-User-Id"));
        return ResponseEntity.ok(Map.of("success", true, "data", app));
    }
}
