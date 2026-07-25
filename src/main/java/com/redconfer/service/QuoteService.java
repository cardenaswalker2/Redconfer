package com.redconfer.service;

import com.redconfer.model.Quote;
import com.redconfer.model.Settings;
import com.redconfer.model.WorkProject;
import com.redconfer.repository.QuoteRepository;
import com.redconfer.repository.SettingsRepository;
import com.redconfer.repository.WorkProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QuoteService {

    private final QuoteRepository quoteRepository;
    private final WorkProjectRepository workProjectRepository;
    private final SettingsRepository settingsRepository;

    public List<Quote> getAllQuotes() {
        return quoteRepository.findAll();
    }

    public Optional<Quote> getQuoteById(String id) {
        return quoteRepository.findById(id);
    }

    public Quote createQuoteRequest(Quote quote) {
        quote.setQuoteNumber("RC-" + LocalDateTime.now().getYear() + "-" + String.format("%04d", (quoteRepository.count() + 1)));
        quote.setStatus(Quote.QuoteStatus.PENDING);
        
        Settings settingsObj = settingsRepository.findFirstByOrderByIdAsc().orElse(null);
        double currentTaxRate = settingsObj != null ? settingsObj.getTaxRate() : 19.0;
        quote.setTaxRate(currentTaxRate);
        
        quote.setCreatedAt(LocalDateTime.now());
        quote.setUpdatedAt(LocalDateTime.now());
        
        Quote.StatusHistory history = new Quote.StatusHistory(
                Quote.QuoteStatus.PENDING, 
                "Solicitud de cotización creada por el cliente.", 
                LocalDateTime.now(), 
                "Sistema"
        );
        quote.getHistory().add(history);
        
        return quoteRepository.save(quote);
    }

    public Quote updateQuoteDetailsAndRecalculate(String id, List<Quote.QuoteItem> items, double discount, String observations, Quote.QuoteStatus status, String crmStage, double materialCost, double laborCost, double transportCost, String updaterName) {
        Quote quote = quoteRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Cotización no encontrada"));
        
        quote.setItems(items);
        double subtotal = items.stream().mapToDouble(Quote.QuoteItem::getTotal).sum();
        quote.setSubtotal(subtotal);
        quote.setDiscountAmount(discount);
        
        double taxAmount = (subtotal - discount) * (quote.getTaxRate() / 100.0);
        quote.setTaxAmount(taxAmount);
        quote.setTotal(subtotal - discount + taxAmount);
        
        quote.setAdminObservations(observations);
        quote.setCrmStage(crmStage);
        quote.setMaterialCost(materialCost);
        quote.setLaborCost(laborCost);
        quote.setTransportCost(transportCost);
        
        double baseNetPrice = subtotal - discount;
        double totalCost = materialCost + laborCost + transportCost;
        double profit = baseNetPrice - totalCost;
        quote.setProfitAmount(profit);
        quote.setMarginPercent(baseNetPrice > 0 ? (profit * 100.0) / baseNetPrice : 0.0);
        
        if (quote.getStatus() != status) {
            quote.setStatus(status);
            Quote.StatusHistory history = new Quote.StatusHistory(
                    status, 
                    "Estado actualizado a: " + status.name() + ". Observaciones: " + observations, 
                    LocalDateTime.now(), 
                    updaterName
            );
            quote.getHistory().add(history);
            
            // Auto create execution project if approved or completed
            if (status == Quote.QuoteStatus.APPROVED || status == Quote.QuoteStatus.COMPLETED) {
                if (workProjectRepository.findByQuoteId(id).isEmpty()) {
                    WorkProject project = WorkProject.builder()
                            .projectId("PRJ-" + LocalDateTime.now().getYear() + "-" + String.format("%04d", (workProjectRepository.count() + 1)))
                            .quoteId(id)
                            .name("Proyecto: " + quote.getServiceType() + " - " + quote.getClientName())
                            .clientId(quote.getClientId())
                            .clientName(quote.getClientName())
                            .description(quote.getNotes())
                            .status(WorkProject.ProjectStatus.PLANNING)
                            .progressPercentage(0)
                            .budget(new WorkProject.ProjectBudget(quote.getTotal(), totalCost, quote.getTotal(), profit))
                            .startDate(LocalDateTime.now())
                            .endDate(LocalDateTime.now().plusDays(7))
                            .build();
                    workProjectRepository.save(project);
                }
            }
        }
        
        quote.setUpdatedAt(LocalDateTime.now());
        return quoteRepository.save(quote);
    }
}
