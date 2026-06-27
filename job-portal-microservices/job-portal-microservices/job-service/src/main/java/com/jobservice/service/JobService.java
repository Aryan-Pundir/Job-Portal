package com.jobservice.service;

import com.jobservice.dto.JobRequest;
import com.jobservice.kafka.JobKafkaProducer;
import com.jobservice.kafka.JobPostedEvent;
import com.jobservice.model.Job;
import com.jobservice.repository.JobRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class JobService {

    @Autowired private JobRepository jobRepository;
    @Autowired private JobKafkaProducer kafkaProducer;

    public Job createJob(JobRequest request, String employerId, String employerName, String employerEmail, String companyName) {
        Job job = Job.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .companyName(companyName != null ? companyName : employerName)
                .location(request.getLocation())
                .jobType(request.getJobType())
                .experienceLevel(request.getExperienceLevel())
                .salaryRange(request.getSalaryRange())
                .skillsRequired(request.getSkillsRequired())
                .status("OPEN")
                .postedById(employerId)
                .postedByName(employerName)
                .postedByEmail(employerEmail)
                .deadline(request.getDeadline())
                .postedAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Job saved = jobRepository.save(job);

        // Publish to Kafka — Notification Service will pick this up
        kafkaProducer.publishJobPostedEvent(JobPostedEvent.builder()
                .jobId(saved.getId())
                .jobTitle(saved.getTitle())
                .companyName(saved.getCompanyName())
                .location(saved.getLocation())
                .jobType(saved.getJobType())
                .experienceLevel(saved.getExperienceLevel())
                .salaryRange(saved.getSalaryRange())
                .postedByEmail(employerEmail)
                .postedByName(employerName)
                .build());

        return saved;
    }

    public List<Job> getAllOpenJobs() {
        return jobRepository.findByStatus("OPEN");
    }

    public Job getJobById(String id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Job not found: " + id));
    }

    public List<Job> searchJobs(String title, String location, String jobType, String experienceLevel) {
        return jobRepository.findByStatus("OPEN").stream()
                .filter(j -> title == null || j.getTitle().toLowerCase().contains(title.toLowerCase()))
                .filter(j -> location == null || j.getLocation().toLowerCase().contains(location.toLowerCase()))
                .filter(j -> jobType == null || jobType.equalsIgnoreCase(j.getJobType()))
                .filter(j -> experienceLevel == null || experienceLevel.equalsIgnoreCase(j.getExperienceLevel()))
                .collect(Collectors.toList());
    }

    public List<Job> getMyJobs(String employerId) {
        return jobRepository.findByPostedById(employerId);
    }

    public Job updateJob(String jobId, JobRequest request, String employerId) {
        Job job = getJobById(jobId);
        if (!job.getPostedById().equals(employerId))
            throw new RuntimeException("Not authorized to update this job");
        job.setTitle(request.getTitle());
        job.setDescription(request.getDescription());
        job.setLocation(request.getLocation());
        job.setJobType(request.getJobType());
        job.setExperienceLevel(request.getExperienceLevel());
        job.setSalaryRange(request.getSalaryRange());
        job.setSkillsRequired(request.getSkillsRequired());
        job.setDeadline(request.getDeadline());
        job.setUpdatedAt(LocalDateTime.now());
        return jobRepository.save(job);
    }

    public void deleteJob(String jobId, String employerId) {
        Job job = getJobById(jobId);
        if (!job.getPostedById().equals(employerId))
            throw new RuntimeException("Not authorized to delete this job");
        jobRepository.delete(job);
    }

    public Job closeJob(String jobId, String employerId) {
        Job job = getJobById(jobId);
        if (!job.getPostedById().equals(employerId))
            throw new RuntimeException("Not authorized to close this job");
        job.setStatus("CLOSED");
        job.setUpdatedAt(LocalDateTime.now());
        return jobRepository.save(job);
    }
}
