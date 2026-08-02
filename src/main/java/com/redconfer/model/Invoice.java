package com.redconfer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "invoices")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Invoice {
    @Id
    private String id;
    private String invoiceNumber; // Auto-generated e.g. FAC-2026-0001
    
    private String clientId; // Optional links to User or ClientCard
    
    // Client basic details
    private String clientName;
    private String clientNit;
    private String clientAddress;
    private String clientCity;
    private String clientPhone;
    private String clientEmail;
    private String observations;
    
    // Invoice info
    private LocalDate issueDate;
    private LocalDate dueDate;
    private String paymentMethod; // e.g. Transferencia, Efectivo, Tarjeta
    private String paymentForm; // e.g. Contado, Crédito 15 días, Crédito 30 días
    
    @Builder.Default
    private InvoiceStatus status = InvoiceStatus.PENDIENTE; // PENDIENTE, PAGADA, VENCIDA, ANULADA
    
    @Builder.Default
    private String currency = "COP"; // default COP
    private String seller; // optional
    
    @Builder.Default
    private List<InvoiceItem> items = new ArrayList<>();
    
    private double subtotal;
    private double discountAmount;
    private double taxAmount; // sum of IVA
    private double total;
    private double paidAmount;
    private double dueAmount; // total - paidAmount
    
    private String totalInWords; // e.g., "UN MILLÓN DOSCIENTOS MIL PESOS M/CTE"
    
    @CreatedDate
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public enum InvoiceStatus {
        PENDIENTE, PAGADA, VENCIDA, ANULADA
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InvoiceItem {
        private String code;
        private String name;
        private String description;
        private int quantity;
        private String unit; // e.g. Unidad, Metro, Global
        private double unitPrice;
        private double discountPercent; // e.g. 5 for 5%
        private double taxRate; // e.g. 19 for 19%
        private double subtotal; // quantity * unitPrice * (1 - discountPercent/100)
    }
}
