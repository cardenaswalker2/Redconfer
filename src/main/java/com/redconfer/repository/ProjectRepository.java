package com.redconfer.repository;

import com.redconfer.model.Project;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends MongoRepository<Project, String> {
    List<Project> findByActiveTrue();
    List<Project> findByCategoryAndActiveTrue(String category);
}
