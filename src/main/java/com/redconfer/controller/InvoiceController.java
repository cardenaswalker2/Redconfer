package com.redconfer.controller;

import com.redconfer.model.*;
import com.redconfer.repository.*;
import com.redconfer.service.PdfService;
import com.redconfer.util.NumberToWordsConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/facturas")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;
    private final ClientCardRepository clientCardRepository;
    private final QuoteRepository quoteRepository;
    private final InventoryRepository inventoryRepository;
    private final ServiceRepository serviceRepository;
    private final TransactionRepository transactionRepository;
    private final SettingsRepository settingsRepository;
    private final PdfService pdfService;

    @ModelAttribute
    public void addAdminAttributes(Model model) {
        model.addAttribute("settings", settingsRepository.findFirstByOrderByIdAsc().orElse(null));
    }

    @GetMapping
    public String listInvoices(Model model,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "startDate", required = false) String startDateStr,
            @RequestParam(value = "endDate", required = false) String endDateStr) {
        List<Invoice> invoices = invoiceRepository.findAll();

        // Filter by search (client name or invoice number)
        if (search != null && !search.trim().isEmpty()) {
            String query = search.toLowerCase().trim();
            invoices = invoices.stream()
                    .filter(i -> (i.getClientName() != null && i.getClientName().toLowerCase().contains(query))
                            || (i.getInvoiceNumber() != null && i.getInvoiceNumber().toLowerCase().contains(query)))
                    .collect(Collectors.toList());
        }

        // Filter by status
        if (status != null && !status.trim().isEmpty()) {
            try {
                Invoice.InvoiceStatus targetStatus = Invoice.InvoiceStatus.valueOf(status.trim().toUpperCase());
                invoices = invoices.stream()
                        .filter(i -> i.getStatus() == targetStatus)
                        .collect(Collectors.toList());
            } catch (IllegalArgumentException ignored) {
            }
        }

        // Filter by dates
        if (startDateStr != null && !startDateStr.isEmpty() && endDateStr != null && !endDateStr.isEmpty()) {
            try {
                LocalDate start = LocalDate.parse(startDateStr);
                LocalDate end = LocalDate.parse(endDateStr);
                invoices = invoices.stream()
                        .filter(i -> !i.getIssueDate().isBefore(start) && !i.getIssueDate().isAfter(end))
                        .collect(Collectors.toList());
            } catch (Exception ignored) {
            }
        }

        // Sort by issue date descending, then by invoice number descending
        invoices.sort((i1, i2) -> {
            int comp = i2.getIssueDate().compareTo(i1.getIssueDate());
            if (comp == 0 && i2.getInvoiceNumber() != null && i1.getInvoiceNumber() != null) {
                return i2.getInvoiceNumber().compareTo(i1.getInvoiceNumber());
            }
            return comp;
        });

        double totalCollected = invoices.stream()
                .filter(i -> i.getStatus() == Invoice.InvoiceStatus.PAGADA)
                .mapToDouble(Invoice::getTotal)
                .sum();

        double dueAmountFiltered = invoices.stream()
                .filter(i -> i.getStatus() == Invoice.InvoiceStatus.PENDIENTE)
                .mapToDouble(Invoice::getDueAmount)
                .sum();

        long cancelledCount = invoices.stream()
                .filter(i -> i.getStatus() == Invoice.InvoiceStatus.ANULADA)
                .count();

        model.addAttribute("invoices", invoices);
        model.addAttribute("totalCollected", totalCollected);
        model.addAttribute("dueAmountFiltered", dueAmountFiltered);
        model.addAttribute("cancelledCount", cancelledCount);
        model.addAttribute("search", search);
        model.addAttribute("statusFilter", status);
        model.addAttribute("startDate", startDateStr);
        model.addAttribute("endDate", endDateStr);
        return "admin/invoices/list";
    }

    @GetMapping("/nuevo")
    public String newInvoiceForm(Model model) {
        Invoice invoice = new Invoice();
        invoice.setIssueDate(LocalDate.now());
        invoice.setDueDate(LocalDate.now().plusDays(30));

        // Generate automatic invoice number
        long count = invoiceRepository.count();
        invoice.setInvoiceNumber("FAC-" + LocalDate.now().getYear() + "-" + String.format("%04d", count + 1));

        model.addAttribute("invoice", invoice);
        model.addAttribute("clients", getCombinedClients());
        model.addAttribute("inventory", inventoryRepository.findAll());
        model.addAttribute("services", serviceRepository.findAll());
        model.addAttribute("isNew", true);
        return "admin/invoices/form";
    }

    @GetMapping("/editar/{id}")
    public String editInvoiceForm(@PathVariable String id, Model model) {
        Invoice invoice = invoiceRepository.findById(id).orElseThrow();
        if (invoice.getStatus() == Invoice.InvoiceStatus.PAGADA
                || invoice.getStatus() == Invoice.InvoiceStatus.ANULADA) {
            return "redirect:/admin/facturas/ver/" + id;
        }
        model.addAttribute("invoice", invoice);
        model.addAttribute("clients", getCombinedClients());
        model.addAttribute("inventory", inventoryRepository.findAll());
        model.addAttribute("services", serviceRepository.findAll());
        model.addAttribute("isNew", false);
        return "admin/invoices/form";
    }

    @GetMapping("/duplicar/{id}")
    public String duplicateInvoiceForm(@PathVariable String id, Model model) {
        Invoice existing = invoiceRepository.findById(id).orElseThrow();
        Invoice duplicate = new Invoice();

        duplicate.setClientId(existing.getClientId());
        duplicate.setClientName(existing.getClientName());
        duplicate.setClientNit(existing.getClientNit());
        duplicate.setClientAddress(existing.getClientAddress());
        duplicate.setClientCity(existing.getClientCity());
        duplicate.setClientPhone(existing.getClientPhone());
        duplicate.setClientEmail(existing.getClientEmail());
        duplicate.setObservations(existing.getObservations());
        duplicate.setPaymentMethod(existing.getPaymentMethod());
        duplicate.setPaymentForm(existing.getPaymentForm());
        duplicate.setCurrency(existing.getCurrency());
        duplicate.setSeller(existing.getSeller());

        // Copy items
        List<Invoice.InvoiceItem> items = new ArrayList<>();
        for (Invoice.InvoiceItem item : existing.getItems()) {
            items.add(Invoice.InvoiceItem.builder()
                    .code(item.getCode())
                    .name(item.getName())
                    .description(item.getDescription())
                    .quantity(item.getQuantity())
                    .unit(item.getUnit())
                    .unitPrice(item.getUnitPrice())
                    .discountPercent(item.getDiscountPercent())
                    .taxRate(item.getTaxRate())
                    .subtotal(item.getSubtotal())
                    .build());
        }
        duplicate.setItems(items);

        duplicate.setSubtotal(existing.getSubtotal());
        duplicate.setDiscountAmount(existing.getDiscountAmount());
        duplicate.setTaxAmount(existing.getTaxAmount());
        duplicate.setTotal(existing.getTotal());
        duplicate.setPaidAmount(0.0);
        duplicate.setDueAmount(existing.getTotal());
        duplicate.setTotalInWords(existing.getTotalInWords());

        duplicate.setIssueDate(LocalDate.now());
        duplicate.setDueDate(LocalDate.now().plusDays(30));

        long count = invoiceRepository.count();
        duplicate.setInvoiceNumber("FAC-" + LocalDate.now().getYear() + "-" + String.format("%04d", count + 1));

        model.addAttribute("invoice", duplicate);
        model.addAttribute("clients", getCombinedClients());
        model.addAttribute("inventory", inventoryRepository.findAll());
        model.addAttribute("services", serviceRepository.findAll());
        model.addAttribute("isNew", true);
        return "admin/invoices/form";
    }

    @PostMapping("/guardar")
    public String saveInvoice(@ModelAttribute Invoice formInvoice,
            @RequestParam(value = "itemCode", required = false) String[] itemCodes,
            @RequestParam(value = "itemName", required = false) String[] itemNames,
            @RequestParam(value = "itemDesc", required = false) String[] itemDescs,
            @RequestParam(value = "itemQty", required = false) int[] itemQties,
            @RequestParam(value = "itemUnit", required = false) String[] itemUnits,
            @RequestParam(value = "itemPrice", required = false) double[] itemPrices,
            @RequestParam(value = "itemDiscount", required = false) double[] itemDiscounts,
            @RequestParam(value = "itemTax", required = false) double[] itemTaxes) {

        Invoice invoice;
        if (formInvoice.getId() != null && !formInvoice.getId().trim().isEmpty()) {
            invoice = invoiceRepository.findById(formInvoice.getId()).orElseThrow();
            if (invoice.getStatus() == Invoice.InvoiceStatus.PAGADA
                    || invoice.getStatus() == Invoice.InvoiceStatus.ANULADA) {
                return "redirect:/admin/facturas/ver/" + invoice.getId();
            }
            invoice.setUpdatedAt(LocalDateTime.now());
        } else {
            invoice = new Invoice();
            invoice.setCreatedAt(LocalDateTime.now());
            invoice.setUpdatedAt(LocalDateTime.now());
            invoice.setStatus(
                    formInvoice.getStatus() != null ? formInvoice.getStatus() : Invoice.InvoiceStatus.PENDIENTE);
        }

        // Map basic properties
        invoice.setInvoiceNumber(formInvoice.getInvoiceNumber());
        invoice.setClientId(formInvoice.getClientId());
        invoice.setClientName(formInvoice.getClientName());
        invoice.setClientNit(formInvoice.getClientNit());
        invoice.setClientAddress(formInvoice.getClientAddress());
        invoice.setClientCity(formInvoice.getClientCity());
        invoice.setClientPhone(formInvoice.getClientPhone());
        invoice.setClientEmail(formInvoice.getClientEmail());
        invoice.setObservations(formInvoice.getObservations());
        invoice.setIssueDate(formInvoice.getIssueDate());
        invoice.setDueDate(formInvoice.getDueDate());
        invoice.setPaymentMethod(formInvoice.getPaymentMethod());
        invoice.setPaymentForm(formInvoice.getPaymentForm());
        invoice.setCurrency(formInvoice.getCurrency() != null ? formInvoice.getCurrency() : "COP");
        invoice.setSeller(formInvoice.getSeller());

        // Re-read status if new
        if (formInvoice.getStatus() != null) {
            invoice.setStatus(formInvoice.getStatus());
        }

        // Build items list
        List<Invoice.InvoiceItem> items = new ArrayList<>();
        double subtotal = 0.0;
        double taxAmount = 0.0;
        double discountAmount = 0.0;

        if (itemCodes != null) {
            for (int i = 0; i < itemCodes.length; i++) {
                if (itemNames[i] == null || itemNames[i].trim().isEmpty())
                    continue;

                int qty = itemQties[i];
                double unitPrice = itemPrices[i];
                double discPercent = itemDiscounts[i];
                double taxRate = itemTaxes[i];

                double itemSubtotalBeforeDiscount = qty * unitPrice;
                double itemDiscount = itemSubtotalBeforeDiscount * (discPercent / 100.0);
                double itemBase = itemSubtotalBeforeDiscount - itemDiscount;
                double itemTax = itemBase * (taxRate / 100.0);
                double itemTotal = itemBase; // item subtotal stored in DB is base after discount

                subtotal += itemSubtotalBeforeDiscount;
                discountAmount += itemDiscount;
                taxAmount += itemTax;

                items.add(Invoice.InvoiceItem.builder()
                        .code(itemCodes[i])
                        .name(itemNames[i])
                        .description(itemDescs[i])
                        .quantity(qty)
                        .unit(itemUnits[i])
                        .unitPrice(unitPrice)
                        .discountPercent(discPercent)
                        .taxRate(taxRate)
                        .subtotal(itemBase) // subtotal in DB is quantity * price * (1 - discountPercent/100)
                        .build());
            }
        }
        invoice.setItems(items);

        invoice.setSubtotal(subtotal);
        invoice.setDiscountAmount(discountAmount);
        invoice.setTaxAmount(taxAmount);
        double total = subtotal - discountAmount + taxAmount;
        invoice.setTotal(total);

        // Adjust paid and due amounts
        if (invoice.getStatus() == Invoice.InvoiceStatus.PAGADA) {
            invoice.setPaidAmount(total);
            invoice.setDueAmount(0.0);
        } else {
            invoice.setPaidAmount(formInvoice.getPaidAmount());
            invoice.setDueAmount(total - formInvoice.getPaidAmount());
        }

        // Generate total in words
        invoice.setTotalInWords(NumberToWordsConverter.convert(total));

        invoiceRepository.save(invoice);

        // Log transaction if paid
        if (invoice.getStatus() == Invoice.InvoiceStatus.PAGADA) {
            logFinanceTransaction(invoice);
        }

        return "redirect:/admin/facturas/ver/" + invoice.getId();
    }

    @GetMapping("/ver/{id}")
    public String viewInvoice(@PathVariable String id, Model model) {
        Invoice invoice = invoiceRepository.findById(id).orElseThrow();
        model.addAttribute("invoice", invoice);
        return "admin/invoices/detail";
    }

    @GetMapping("/pdf/{id}")
    public ResponseEntity<byte[]> downloadInvoicePdf(@PathVariable String id) {
        Invoice invoice = invoiceRepository.findById(id).orElseThrow();
        byte[] pdfBytes = pdfService.generateInvoicePdf(invoice);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=Factura_" + invoice.getInvoiceNumber() + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping("/preview/{id}")
    public String previewInvoiceHtml(@PathVariable String id, Model model) {
        Invoice invoice = invoiceRepository.findById(id).orElseThrow();
        model.addAttribute("invoice", invoice);
        return "admin/invoices/pdf_template";
    }

    @PostMapping("/pagar/{id}")
    public String markAsPaid(@PathVariable String id) {
        Invoice invoice = invoiceRepository.findById(id).orElseThrow();
        if (invoice.getStatus() != Invoice.InvoiceStatus.PAGADA
                && invoice.getStatus() != Invoice.InvoiceStatus.ANULADA) {
            invoice.setStatus(Invoice.InvoiceStatus.PAGADA);
            invoice.setPaidAmount(invoice.getTotal());
            invoice.setDueAmount(0.0);
            invoice.setUpdatedAt(LocalDateTime.now());
            invoiceRepository.save(invoice);
            logFinanceTransaction(invoice);
        }
        return "redirect:/admin/facturas/ver/" + id;
    }

    @PostMapping("/anular/{id}")
    public String voidInvoice(@PathVariable String id) {
        Invoice invoice = invoiceRepository.findById(id).orElseThrow();
        if (invoice.getStatus() != Invoice.InvoiceStatus.ANULADA) {
            invoice.setStatus(Invoice.InvoiceStatus.ANULADA);
            invoice.setDueAmount(0.0);
            invoice.setPaidAmount(0.0);
            invoice.setUpdatedAt(LocalDateTime.now());
            invoiceRepository.save(invoice);
        }
        return "redirect:/admin/facturas/ver/" + id;
    }

    // --- API ENDPOINTS FOR AUTOCOMPLETE AND AJAX ---

    @GetMapping("/api/quotes")
    @ResponseBody
    public List<Map<String, Object>> getQuotesByClient(@RequestParam("clientId") String clientId) {
        List<Quote> quotes = quoteRepository.findByClientId(clientId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Quote q : quotes) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", q.getId());
            map.put("quoteNumber", q.getQuoteNumber());
            map.put("total", q.getTotal());
            map.put("serviceType", q.getServiceType());
            map.put("date", q.getCreatedAt() != null ? q.getCreatedAt().toLocalDate().toString() : "");
            result.add(map);
        }
        return result;
    }

    @GetMapping("/api/quotes/{id}")
    @ResponseBody
    public Map<String, Object> getQuoteDetails(@PathVariable String id) {
        Quote quote = quoteRepository.findById(id).orElseThrow();
        Map<String, Object> result = new HashMap<>();
        result.put("subtotal", quote.getSubtotal());
        result.put("taxRate", quote.getTaxRate());
        result.put("taxAmount", quote.getTaxAmount());
        result.put("discountAmount", quote.getDiscountAmount());
        result.put("total", quote.getTotal());
        result.put("observations", quote.getAdminObservations());

        List<Map<String, Object>> items = new ArrayList<>();
        if (quote.getItems() != null) {
            for (Quote.QuoteItem item : quote.getItems()) {
                Map<String, Object> itemMap = new HashMap<>();
                itemMap.put("code", "SERV");
                itemMap.put("name", quote.getServiceType() != null ? quote.getServiceType() : "Servicios Técnicos");
                itemMap.put("description", item.getDescription());
                itemMap.put("quantity", item.getQuantity());
                itemMap.put("unit", "Global");
                itemMap.put("unitPrice", item.getUnitPrice());
                itemMap.put("discountPercent", 0.0);
                itemMap.put("taxRate", quote.getTaxRate());
                itemMap.put("subtotal", item.getTotal());
                items.add(itemMap);
            }
        }
        result.put("items", items);
        return result;
    }

    // Helpers

    private List<Map<String, String>> getCombinedClients() {
        List<Map<String, String>> clientList = new ArrayList<>();

        // Add from ClientCard
        for (ClientCard cc : clientCardRepository.findAll()) {
            Map<String, String> map = new HashMap<>();
            map.put("id", cc.getId());
            map.put("name", cc.getName());
            map.put("nit", cc.getNit() != null ? cc.getNit() : "");
            map.put("address", cc.getAddress() != null ? cc.getAddress() : "");
            map.put("city", cc.getCity() != null ? cc.getCity() : "");
            map.put("phone", cc.getPhones().isEmpty() ? "" : cc.getPhones().get(0));
            map.put("email", cc.getEmails().isEmpty() ? "" : cc.getEmails().get(0));
            clientList.add(map);
        }

        // Add from User (ROLE_CUSTOMER or ROLE_USER, filter admins)
        for (User u : userRepository.findAll()) {
            if (u.getRole() != UserRole.ROLE_ADMIN) {
                // Check if already in the list to prevent duplicate clients by name/email
                boolean exists = clientList.stream().anyMatch(c -> c.get("email").equalsIgnoreCase(u.getUsername()));
                if (!exists) {
                    Map<String, String> map = new HashMap<>();
                    map.put("id", u.getId());
                    map.put("name", u.getName());
                    map.put("nit", "");
                    map.put("address", u.getAddress() != null ? u.getAddress() : "");
                    map.put("city", u.getCity() != null ? u.getCity() : "");
                    map.put("phone", u.getPhone() != null ? u.getPhone() : "");
                    map.put("email", u.getUsername());
                    clientList.add(map);
                }
            }
        }

        clientList.sort(Comparator.comparing(m -> m.get("name")));
        return clientList;
    }

    private void logFinanceTransaction(Invoice invoice) {
        // Check if already logged to prevent double posting
        List<Transaction> existing = transactionRepository.findAll();
        boolean alreadyLogged = existing.stream()
                .anyMatch(t -> t.getDescription() != null
                        && t.getDescription().contains("Pago de Factura N° " + invoice.getInvoiceNumber()));

        if (!alreadyLogged) {
            Transaction tx = Transaction.builder()
                    .type("INCOME")
                    .category("Facturación")
                    .amount(invoice.getTotal())
                    .description("Pago de Factura N° " + invoice.getInvoiceNumber() + " - Cliente: "
                            + invoice.getClientName())
                    .timestamp(LocalDateTime.now())
                    .build();
            transactionRepository.save(tx);
        }
    }
}
