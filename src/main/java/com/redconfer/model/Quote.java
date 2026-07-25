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

@Document(collection = "quotes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Quote {
    @Id
    private String id;
    private String quoteNumber; // Auto-generated e.g. RC-2026-0001
    
    private String clientId; // Optional, links to User
    
    // Client basic details
    private String clientName;
    private String clientEmail;
    private String clientPhone;
    private String clientCompany;
    private String clientAddress;
    private String clientCity;
    
    // Technical requirement fields
    private String serviceType; // e.g. CCTV, Access Control
    private String propertyType; // e.g. Residential, Industrial, Commercial
    private String sizeSqMeters;
    private int camerasQty; // for coverage calc
    private String notes;
    private List<String> clientPhotos; // Client uploads for estimation
    
    // Line items added by Admin
    @Builder.Default
    private List<QuoteItem> items = new ArrayList<>();
    
    private double subtotal;
    private double taxRate; // e.g. 19 for 19% IVA
    private double taxAmount;
    private double discountAmount;
    private double total;

    // Advanced CRM & Financial fields
    @Builder.Default
    private String crmStage = "CONTACT"; // CONTACT, ESCRIBI, RESPONDIO, INTERESADO, SIN_RESPUESTA, NEGOCIACION, GANADO, PERDIDO
    private double materialCost;
    private double laborCost;
    private double transportCost;
    private double marginPercent;
    private double profitAmount;
    
    private String adminObservations;
    private String termsAndConditions;
    
    // Oportunidades & CRM parameters
    private String priority; // BAJA, MEDIA, ALTA, URGENTE
    private String howMet; // Origin: FACEBOOK, INSTAGRAM, GOOGLE, WHATSAPP, etc.
    private LocalDateTime nextFollowUpDate;
    
    @Builder.Default
    private List<CrmActivity> activities = new ArrayList<>();
    
    @Builder.Default
    private QuoteStatus status = QuoteStatus.PENDING;
    
    // Technical visit details
    private String assignedTechnicianId; // Links to User (Employee role)
    private LocalDateTime scheduledVisitDate;
    
    // Installation status tracker (for real time progress)
    private InstallationStep currentInstallationStep;
    
    // History log of state changes
    @Builder.Default
    private List<StatusHistory> history = new ArrayList<>();
    
    @CreatedDate
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum QuoteStatus {
        PENDING, IN_REVIEW, SENT, APPROVED, REJECTED, COMPLETED
    }

    public enum InstallationStep {
        PLANNING, WIRING, MOUNTING, CONFIGURATION, QUALITY_ASSURANCE, COMPLETED
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuoteItem {
        private String description;
        private int quantity;
        private double unitPrice;
        private double total;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusHistory {
        private QuoteStatus status;
        private String comment;
        private LocalDateTime timestamp;
        private String updatedBy;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CrmActivity {
        private String id;
        private String type; // e.g. CALL, WHATSAPP, EMAIL, VISIT, NOTE, etc.
        private String text;
        private LocalDateTime timestamp;
        private String author;
    }
}
