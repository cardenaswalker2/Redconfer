package com.redconfer.repository;

import com.redconfer.model.AuditLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends MongoRepository<AuditLog, String> {
    List<AuditLog> findByUsername(String username);
    List<AuditLog> findByAction(String action);
}
