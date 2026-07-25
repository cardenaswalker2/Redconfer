package com.redconfer.repository;

import com.redconfer.model.Quote;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuoteRepository extends MongoRepository<Quote, String> {
    Optional<Quote> findByQuoteNumber(String quoteNumber);
    List<Quote> findByClientId(String clientId);
    List<Quote> findByAssignedTechnicianId(String assignedTechnicianId);
    List<Quote> findByStatus(Quote.QuoteStatus status);
}
