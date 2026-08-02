package com.redconfer.repository;

import com.redconfer.model.Invoice;
import com.redconfer.model.Invoice.InvoiceStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends MongoRepository<Invoice, String> {
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    List<Invoice> findByClientNameContainingIgnoreCase(String clientName);
    List<Invoice> findByStatus(InvoiceStatus status);
    List<Invoice> findByIssueDateBetween(LocalDate startDate, LocalDate endDate);
}
