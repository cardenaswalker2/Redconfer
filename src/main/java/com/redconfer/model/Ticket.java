package com.redconfer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "tickets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {
    @Id
    private String id;
    private String ticketNumber; // e.g. TK-0021
    private String clientId;
    private String clientName;
    private String subject;
    private String description;
    
    @Builder.Default
    private TicketStatus status = TicketStatus.OPEN;
    @Builder.Default
    private TicketPriority priority = TicketPriority.MEDIUM;
    
    private String category; // e.g. "Cámaras", "Redes", "Facturación"
    
    @Builder.Default
    private List<TicketMessage> messages = new ArrayList<>();
    
    @CreatedDate
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum TicketStatus {
        OPEN, IN_PROGRESS, RESOLVED, CLOSED
    }

    public enum TicketPriority {
        LOW, MEDIUM, HIGH, URGENT
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TicketMessage {
        private String senderName;
        private String senderRole; // Admin, Client, Employee
        private String message;
        private LocalDateTime timestamp;
    }
}
