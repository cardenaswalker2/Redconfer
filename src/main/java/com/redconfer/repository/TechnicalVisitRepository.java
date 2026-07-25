package com.redconfer.repository;

import com.redconfer.model.TechnicalVisit;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TechnicalVisitRepository extends MongoRepository<TechnicalVisit, String> {
    List<TechnicalVisit> findByAssignedTechnicianId(String technicianId);
    List<TechnicalVisit> findByStatus(TechnicalVisit.VisitStatus status);
}
