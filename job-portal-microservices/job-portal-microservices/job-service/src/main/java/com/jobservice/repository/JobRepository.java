package com.jobservice.repository;

import com.jobservice.model.Job;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends MongoRepository<Job, String> {
    List<Job> findByPostedById(String postedById);
    List<Job> findByStatus(String status);
}
