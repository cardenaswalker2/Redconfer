package com.redconfer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "technical_visits")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TechnicalVisit {
    @Id
    private String id;
    private String quoteId; // Links to Quote
    private String clientName;
    private String clientPhone;
    private String clientAddress;
    private String clientCity;
    
    private String assignedTechnicianId; // Links to User (Employee role)
    private String assignedTechnicianName;
    
    private LocalDateTime scheduledDateTime;
    private String description;
    
    @Builder.Default
    private VisitStatus status = VisitStatus.SCHEDULED;
    
    private String notes;

    public enum VisitStatus {
        SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED
    }
}
