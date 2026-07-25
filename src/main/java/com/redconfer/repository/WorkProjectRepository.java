package com.redconfer.repository;

import com.redconfer.model.WorkProject;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkProjectRepository extends MongoRepository<WorkProject, String> {
    Optional<WorkProject> findByProjectId(String projectId);
    Optional<WorkProject> findByQuoteId(String quoteId);
    List<WorkProject> findByClientId(String clientId);
    List<WorkProject> findByStatus(WorkProject.ProjectStatus status);
}
