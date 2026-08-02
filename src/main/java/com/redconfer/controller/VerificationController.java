package com.redconfer.controller;

import com.redconfer.model.Invoice;
import com.redconfer.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class VerificationController {

    private final InvoiceRepository invoiceRepository;

    @GetMapping("/verificar/{invoiceNumber}")
    public String verifyInvoice(@PathVariable String invoiceNumber, Model model) {
        Optional<Invoice> invoiceOpt = invoiceRepository.findByInvoiceNumber(invoiceNumber);
        
        if (invoiceOpt.isPresent()) {
            Invoice invoice = invoiceOpt.get();
            
            // For backward compatibility, populate timeline events if list is empty
            if (invoice.getTimeline() == null || invoice.getTimeline().isEmpty()) {
                invoice.setTimeline(new ArrayList<>());
                
                // Event 1: Creation
                invoice.getTimeline().add(Invoice.InvoiceEvent.builder()
                        .name("Factura creada")
                        .timestamp(invoice.getCreatedAt() != null ? invoice.getCreatedAt() : LocalDateTime.now().minusDays(2))
                        .responsibleUser("Administrador")
                        .completed(true)
                        .build());
                        
                // Event 2: PDF Generated
                invoice.getTimeline().add(Invoice.InvoiceEvent.builder()
                        .name("PDF generado")
                        .timestamp(invoice.getCreatedAt() != null ? invoice.getCreatedAt().plusMinutes(5) : LocalDateTime.now().minusDays(2).plusMinutes(5))
                        .responsibleUser("Sistema")
                        .completed(true)
                        .build());
                        
                // Event 3: Sent to customer
                invoice.getTimeline().add(Invoice.InvoiceEvent.builder()
                        .name("Enviada al cliente")
                        .timestamp(invoice.getCreatedAt() != null ? invoice.getCreatedAt().plusHours(1) : LocalDateTime.now().minusDays(2).plusHours(1))
                        .responsibleUser("Ventas")
                        .completed(true)
                        .build());
                
                // Event 4: Payment registered
                if (invoice.getStatus() == Invoice.InvoiceStatus.PAGADA) {
                    invoice.getTimeline().add(Invoice.InvoiceEvent.builder()
                            .name("Pago registrado")
                            .timestamp(invoice.getUpdatedAt() != null ? invoice.getUpdatedAt() : LocalDateTime.now())
                            .responsibleUser("Administrador")
                            .completed(true)
                            .build());
                    
                    invoice.getTimeline().add(Invoice.InvoiceEvent.builder()
                            .name("Garantía activada")
                            .timestamp(invoice.getUpdatedAt() != null ? invoice.getUpdatedAt().plusMinutes(1) : LocalDateTime.now().plusMinutes(1))
                            .responsibleUser("Sistema de Soporte")
                            .completed(true)
                            .build());
                }
            }
            
            model.addAttribute("invoice", invoice);
            model.addAttribute("found", true);
        } else {
            model.addAttribute("found", false);
            model.addAttribute("invoiceNumber", invoiceNumber);
        }
        
        return "verification";
    }
}
