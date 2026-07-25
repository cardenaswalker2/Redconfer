package com.redconfer.repository;

import com.redconfer.model.Transaction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends MongoRepository<Transaction, String> {
    List<Transaction> findByType(String type);
    List<Transaction> findByProjectId(String projectId);
}
