package com.redconfer.controller;

import com.redconfer.model.*;
import com.redconfer.model.Quote.QuoteItem;
import com.redconfer.repository.*;
import com.redconfer.service.PdfService;
import com.redconfer.service.QuoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final QuoteRepository quoteRepository;
    private final ServiceRepository serviceRepository;
    private final ProjectRepository projectRepository;
    private final BlogPostRepository blogPostRepository;
    private final TicketRepository ticketRepository;
    private final InventoryRepository inventoryRepository;
    private final SettingsRepository settingsRepository;
    private final QuoteService quoteService;
    private final PdfService pdfService;
    private final WorkProjectRepository workProjectRepository;
    private final ClientCardRepository clientCardRepository;
    private final TransactionRepository transactionRepository;

    @ModelAttribute
    public void addAdminAttributes(Model model) {
        model.addAttribute("settings", settingsRepository.findFirstByOrderByIdAsc().orElse(null));
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<Quote> quotes = quoteRepository.findAll();
        List<User> clients = userRepository.findAll().stream()
                .filter(u -> u.getRole() != UserRole.ROLE_ADMIN)
                .collect(Collectors.toList());
        List<Service> services = serviceRepository.findAll();
        List<WorkProject> projects = workProjectRepository.findAll();
        List<Inventory> lowStockItems = inventoryRepository.findAll().stream()
                .filter(item -> item.getStock() < item.getMinStockAlert())
                .collect(Collectors.toList());

        long pendingQuotes = quotes.stream().filter(q -> q.getStatus() == Quote.QuoteStatus.PENDING).count();
        long approvedQuotes = quotes.stream().filter(q -> q.getStatus() == Quote.QuoteStatus.APPROVED).count();
        long activeProjectsCount = projects.stream().filter(p -> p.getStatus() == WorkProject.ProjectStatus.IN_PROGRESS).count();
        
        double totalSales = quotes.stream()
                .filter(q -> q.getStatus() == Quote.QuoteStatus.APPROVED || q.getStatus() == Quote.QuoteStatus.COMPLETED)
                .mapToDouble(Quote::getTotal).sum();

        double totalProfit = quotes.stream()
                .filter(q -> q.getStatus() == Quote.QuoteStatus.APPROVED || q.getStatus() == Quote.QuoteStatus.COMPLETED)
                .mapToDouble(Quote::getProfitAmount).sum();

        double avgMargin = quotes.stream()
                .filter(q -> (q.getStatus() == Quote.QuoteStatus.APPROVED || q.getStatus() == Quote.QuoteStatus.COMPLETED) && q.getTotal() > 0)
                .mapToDouble(Quote::getMarginPercent)
                .average()
                .orElse(0.0);

        String aiMessage = "¡Buenos días, Carlos! Hoy tienes " + pendingQuotes + " cotizaciones pendientes de respuesta y " + 
                activeProjectsCount + " proyectos activos de ingeniería en Cartagena. " + 
                (lowStockItems.isEmpty() ? "El inventario de materiales está al día." : "Atención: tienes " + lowStockItems.size() + " insumos técnicos con stock por debajo del límite mínimo en bodega.");

        model.addAttribute("totalClients", clients.size());
        model.addAttribute("totalQuotes", quotes.size());
        model.addAttribute("pendingQuotes", pendingQuotes);
        model.addAttribute("approvedQuotes", approvedQuotes);
        model.addAttribute("activeProjectsCount", activeProjectsCount);
        model.addAttribute("totalSales", totalSales);
        model.addAttribute("totalProfit", totalProfit);
        model.addAttribute("avgMargin", avgMargin);
        model.addAttribute("aiMessage", aiMessage);
        model.addAttribute("lowStockItems", lowStockItems);
        model.addAttribute("projects", projects);

        model.addAttribute("recentQuotes", quotes.stream()
                .sorted(Comparator.comparing(Quote::getCreatedAt).reversed())
                .limit(5)
                .collect(Collectors.toList()));
        model.addAttribute("recentClients", clients.stream()
                .sorted(Comparator.comparing(User::getCreatedAt).reversed())
                .limit(5)
                .collect(Collectors.toList()));

        List<Double> monthlySales = Arrays.asList(0.0, 0.0, 0.0, 0.0, totalSales);
        model.addAttribute("chartSales", monthlySales);

        return "admin/dashboard";
    }

    // --- SERVICES MANAGEMENT ---
    @GetMapping("/servicios")
    public String listServices(Model model) {
        model.addAttribute("services", serviceRepository.findAll());
        return "admin/services";
    }

    @GetMapping("/servicios/nuevo")
    public String newServiceForm(Model model) {
        model.addAttribute("service", new Service());
        return "admin/service-form";
    }

    @PostMapping("/servicios/guardar")
    public String saveService(@ModelAttribute Service service) {
        if (service.getSlug() == null || service.getSlug().isEmpty()) {
            service.setSlug(service.getName().toLowerCase().replaceAll("[^a-z0-9]", "-"));
        }
        serviceRepository.save(service);
        return "redirect:/admin/servicios";
    }

    @GetMapping("/servicios/editar/{id}")
    public String editServiceForm(@PathVariable String id, Model model) {
        Service service = serviceRepository.findById(id).orElseThrow();
        model.addAttribute("service", service);
        return "admin/service-form";
    }

    @GetMapping("/servicios/eliminar/{id}")
    public String deleteService(@PathVariable String id) {
        serviceRepository.deleteById(id);
        return "redirect:/admin/servicios";
    }

    // --- QUOTES MANAGEMENT ---
    @GetMapping("/cotizaciones")
    public String listQuotes(Model model) {
        model.addAttribute("quotes", quoteRepository.findAll());
        return "admin/quotes";
    }

    @GetMapping("/cotizaciones/ver/{id}")
    public String viewQuote(@PathVariable String id, Model model) {
        Quote quote = quoteRepository.findById(id).orElseThrow();
        model.addAttribute("quote", quote);
        model.addAttribute("statusList", Quote.QuoteStatus.values());
        model.addAttribute("technicians", userRepository.findByRole(UserRole.ROLE_EMPLOYEE));
        return "admin/quote-detail";
    }

    @PostMapping("/cotizaciones/recalcular/{id}")
    public String recalculateQuote(@PathVariable String id,
                                   @RequestParam("clientName") String clientName,
                                   @RequestParam(value = "clientCompany", required = false) String clientCompany,
                                   @RequestParam(value = "clientPhone", required = false) String clientPhone,
                                   @RequestParam(value = "clientEmail", required = false) String clientEmail,
                                   @RequestParam(value = "clientAddress", required = false) String clientAddress,
                                   @RequestParam(value = "clientCity", required = false) String clientCity,
                                   @RequestParam("descItem") String[] descriptions,
                                   @RequestParam("qtyItem") int[] quantities,
                                   @RequestParam("priceItem") double[] prices,
                                   @RequestParam("discount") double discount,
                                   @RequestParam("observations") String observations,
                                   @RequestParam("status") Quote.QuoteStatus status,
                                   @RequestParam("crmStage") String crmStage,
                                   @RequestParam("materialCost") double materialCost,
                                   @RequestParam("laborCost") double laborCost,
                                   @RequestParam("transportCost") double transportCost,
                                   @AuthenticationPrincipal UserDetails userDetails) {
        
        // 1. Update client details directly on the quote
        Quote quote = quoteRepository.findById(id).orElseThrow();
        quote.setClientName(clientName);
        quote.setClientCompany(clientCompany);
        quote.setClientPhone(clientPhone);
        quote.setClientEmail(clientEmail);
        quote.setClientAddress(clientAddress);
        quote.setClientCity(clientCity);
        quoteRepository.save(quote);

        // 2. Synchronize to ClientCard (Ficha de Cliente)
        ClientCard clientCard = null;
        if (clientEmail != null && !clientEmail.trim().isEmpty()) {
            clientCard = clientCardRepository.findAll().stream()
                    .filter(c -> c.getEmails() != null && c.getEmails().contains(clientEmail.trim()))
                    .findFirst().orElse(null);
        }
        if (clientCard == null && clientPhone != null && !clientPhone.trim().isEmpty()) {
            clientCard = clientCardRepository.findAll().stream()
                    .filter(c -> c.getPhones() != null && c.getPhones().contains(clientPhone.trim()))
                    .findFirst().orElse(null);
        }
        if (clientCard == null) {
            clientCard = clientCardRepository.findAll().stream()
                    .filter(c -> c.getName() != null && c.getName().equalsIgnoreCase(clientName.trim()))
                    .findFirst().orElse(null);
        }

        if (clientCard == null) {
            clientCard = new ClientCard();
            clientCard.setName(clientName);
            clientCard.setCompany(clientCompany);
            clientCard.setAddress(clientAddress);
            clientCard.setCity(clientCity);
            clientCard.setEmails(new ArrayList<>());
            clientCard.setPhones(new ArrayList<>());
            if (clientEmail != null && !clientEmail.trim().isEmpty()) {
                clientCard.getEmails().add(clientEmail.trim());
            }
            if (clientPhone != null && !clientPhone.trim().isEmpty()) {
                clientCard.getPhones().add(clientPhone.trim());
            }
            clientCard.setCreatedAt(LocalDateTime.now());
            clientCard.setUpdatedAt(LocalDateTime.now());
            clientCard.setOrigin("CRM / Cotización");
        } else {
            clientCard.setName(clientName);
            if (clientCompany != null && !clientCompany.trim().isEmpty()) {
                clientCard.setCompany(clientCompany);
            }
            if (clientAddress != null && !clientAddress.trim().isEmpty()) {
                clientCard.setAddress(clientAddress);
            }
            if (clientCity != null && !clientCity.trim().isEmpty()) {
                clientCard.setCity(clientCity);
            }
            if (clientCard.getEmails() == null) {
                clientCard.setEmails(new ArrayList<>());
            }
            if (clientCard.getPhones() == null) {
                clientCard.setPhones(new ArrayList<>());
            }
            if (clientEmail != null && !clientEmail.trim().isEmpty() && !clientCard.getEmails().contains(clientEmail.trim())) {
                clientCard.getEmails().add(clientEmail.trim());
            }
            if (clientPhone != null && !clientPhone.trim().isEmpty() && !clientCard.getPhones().contains(clientPhone.trim())) {
                clientCard.getPhones().add(clientPhone.trim());
            }
            clientCard.setUpdatedAt(LocalDateTime.now());
        }
        clientCardRepository.save(clientCard);

        // Update clientId back to the quote
        quote.setClientId(clientCard.getId());
        quoteRepository.save(quote);

        // 3. Recalculate quote items
        List<QuoteItem> items = new ArrayList<>();
        for (int i = 0; i < descriptions.length; i++) {
            if (!descriptions[i].trim().isEmpty()) {
                items.add(new QuoteItem(descriptions[i], quantities[i], prices[i], quantities[i] * prices[i]));
            }
        }

        quoteService.updateQuoteDetailsAndRecalculate(id, items, discount, observations, status, crmStage, materialCost, laborCost, transportCost, userDetails.getUsername());
        return "redirect:/admin/cotizaciones/ver/" + id;
    }

    @GetMapping("/cotizaciones/nuevo")
    public String newQuoteForm(@RequestParam(value = "clientId", required = false) String clientId, Model model) {
        Quote quote = new Quote();
        if (clientId != null && !clientId.isEmpty()) {
            ClientCard client = clientCardRepository.findById(clientId).orElse(null);
            if (client != null) {
                quote.setClientId(client.getId());
                quote.setClientName(client.getName());
                quote.setClientCompany(client.getCompany());
                quote.setClientPhone(client.getPhones().isEmpty() ? "" : client.getPhones().get(0));
                quote.setClientEmail(client.getEmails().isEmpty() ? "" : client.getEmails().get(0));
                quote.setClientAddress(client.getAddress());
                quote.setClientCity(client.getCity());
            }
        }
        model.addAttribute("quote", quote);
        model.addAttribute("statusList", Quote.QuoteStatus.values());
        model.addAttribute("clients", clientCardRepository.findAll());
        return "admin/quote-form";
    }

    @PostMapping("/cotizaciones/crear-manual")
    public String createManualQuote(@ModelAttribute Quote quote,
                                    @RequestParam("descItem") String[] descriptions,
                                    @RequestParam("qtyItem") int[] quantities,
                                    @RequestParam("priceItem") double[] prices,
                                    @RequestParam("discount") double discount,
                                    @RequestParam("observations") String observations,
                                    @RequestParam("status") Quote.QuoteStatus status,
                                    @RequestParam("crmStage") String crmStage,
                                    @RequestParam("materialCost") double materialCost,
                                    @RequestParam("laborCost") double laborCost,
                                    @RequestParam("transportCost") double transportCost,
                                    @AuthenticationPrincipal UserDetails userDetails) {
        List<QuoteItem> items = new ArrayList<>();
        for (int i = 0; i < descriptions.length; i++) {
            if (!descriptions[i].trim().isEmpty()) {
                items.add(new QuoteItem(descriptions[i], quantities[i], prices[i], quantities[i] * prices[i]));
            }
        }
        
        quote.setQuoteNumber("RC-" + LocalDateTime.now().getYear() + "-" + String.format("%04d", (quoteRepository.count() + 1)));
        quote.setItems(items);
        double subtotal = items.stream().mapToDouble(Quote.QuoteItem::getTotal).sum();
        quote.setSubtotal(subtotal);
        quote.setDiscountAmount(discount);
        Settings settingsObj = settingsRepository.findFirstByOrderByIdAsc().orElse(null);
        double currentTaxRate = settingsObj != null ? settingsObj.getTaxRate() : 19.0;
        quote.setTaxRate(currentTaxRate);
        double taxAmount = (subtotal - discount) * (currentTaxRate / 100.0);
        quote.setTaxAmount(taxAmount);
        quote.setTotal(subtotal - discount + taxAmount);
        quote.setAdminObservations(observations);
        quote.setStatus(status);
        quote.setCrmStage(crmStage);
        quote.setMaterialCost(materialCost);
        quote.setLaborCost(laborCost);
        quote.setTransportCost(transportCost);
        
        double baseNetPrice = subtotal - discount;
        double totalCost = materialCost + laborCost + transportCost;
        double profit = baseNetPrice - totalCost;
        quote.setProfitAmount(profit);
        quote.setMarginPercent(baseNetPrice > 0 ? (profit * 100.0) / baseNetPrice : 0.0);
        quote.setCreatedAt(LocalDateTime.now());
        quote.setUpdatedAt(LocalDateTime.now());
        
        quoteRepository.save(quote);
        
        if (status == Quote.QuoteStatus.APPROVED || status == Quote.QuoteStatus.COMPLETED) {
            WorkProject project = WorkProject.builder()
                    .projectId("PRJ-" + LocalDateTime.now().getYear() + "-" + String.format("%04d", (workProjectRepository.count() + 1)))
                    .quoteId(quote.getId())
                    .name("Proyecto: " + quote.getServiceType() + " - " + quote.getClientName())
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
        
        return "redirect:/admin/cotizaciones";
    }

    @GetMapping("/cotizaciones/pdf/{id}")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable String id) {
        Quote quote = quoteRepository.findById(id).orElseThrow();
        byte[] pdfBytes = pdfService.generateQuotePdf(quote);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Cotizacion_" + quote.getQuoteNumber() + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    // --- CLIENTS CARD 360 MANAGEMENT ---
    @GetMapping("/clientes")
    public String listClients(Model model) {
        List<ClientCard> clients = clientCardRepository.findAll();
        System.out.println("=== DEBUG: LOGGING REGISTERED CLIENTS ===");
        for (ClientCard c : clients) {
            System.out.println("Client: " + c.getName() + " | ID: " + c.getId() + " | NIT: " + c.getNit());
        }
        System.out.println("=========================================");
        model.addAttribute("clients", clients);
        return "admin/clients";
    }

    @GetMapping("/clientes/nuevo")
    public String newClientForm(Model model) {
        model.addAttribute("client", new ClientCard());
        return "admin/client-form";
    }

    @GetMapping("/clientes/editar/{id}")
    public String editClientForm(@PathVariable String id, Model model) {
        ClientCard client = clientCardRepository.findById(id).orElseThrow();
        model.addAttribute("client", client);
        return "admin/client-form";
    }

    @PostMapping("/clientes/guardar")
    public String saveClient(@ModelAttribute ClientCard client, 
                             @RequestParam(value = "emailVal", required = false) String emailVal,
                             @RequestParam(value = "phoneVal", required = false) String phoneVal) {
        if (client.getId() != null && !client.getId().isEmpty()) {
            ClientCard existing = clientCardRepository.findById(client.getId()).orElseThrow();
            client.setCreatedAt(existing.getCreatedAt());
            client.setTimeline(existing.getTimeline());
            client.setInstalledEquipment(existing.getInstalledEquipment());
        }
        
        if (emailVal != null && !emailVal.trim().isEmpty()) {
            client.getEmails().add(emailVal.trim());
        }
        if (phoneVal != null && !phoneVal.trim().isEmpty()) {
            client.getPhones().add(phoneVal.trim());
        }
        
        client.setUpdatedAt(LocalDateTime.now());
        clientCardRepository.save(client);
        return "redirect:/admin/clientes";
    }

    @GetMapping("/clientes/ver/")
    public String viewClientDetailEmpty() {
        return "redirect:/admin/clientes";
    }

    @GetMapping("/clientes/ver/{id}")
    public String viewClientDetail(@PathVariable String id, Model model) {
        ClientCard client = clientCardRepository.findById(id).orElseThrow();
        model.addAttribute("client", client);
        return "admin/client-detail";
    }

    @PostMapping("/clientes/save-timeline/{id}")
    public String saveClientTimeline(@PathVariable String id, 
                                     @RequestParam("timelineText") String text,
                                     @AuthenticationPrincipal UserDetails userDetails) {
        ClientCard client = clientCardRepository.findById(id).orElseThrow();
        
        ClientCard.TimelineEntry entry = ClientCard.TimelineEntry.builder()
                .id(UUID.randomUUID().toString())
                .text(text)
                .timestamp(LocalDateTime.now())
                .author(userDetails.getUsername())
                .build();
        
        client.getTimeline().add(entry);
        clientCardRepository.save(client);
        return "redirect:/admin/clientes/ver/" + id;
    }

    @PostMapping("/clientes/save-equipment/{id}")
    public String saveClientEquipment(@PathVariable String id,
                                      @RequestParam("eqName") String name,
                                      @RequestParam("eqSerial") String serial,
                                      @RequestParam("eqLocation") String location,
                                      @RequestParam("eqWarranty") String warranty) {
        ClientCard client = clientCardRepository.findById(id).orElseThrow();
        
        ClientCard.InstalledEquipment eq = ClientCard.InstalledEquipment.builder()
                .id(UUID.randomUUID().toString())
                .name(name)
                .serialNumber(serial)
                .location(location)
                .installDate(LocalDateTime.now())
                .warrantyPeriod(warranty)
                .build();
                
        client.getInstalledEquipment().add(eq);
        clientCardRepository.save(client);
        return "redirect:/admin/clientes/ver/" + id;
    }

    // --- FINANCES & CASH FLOW ---
    @GetMapping("/finanzas")
    public String viewFinances(Model model) {
        List<Transaction> txs = transactionRepository.findAll();
        double totalIncomes = txs.stream().filter(t -> "INCOME".equals(t.getType())).mapToDouble(Transaction::getAmount).sum();
        double totalExpenses = txs.stream().filter(t -> "EXPENSE".equals(t.getType())).mapToDouble(Transaction::getAmount).sum();
        double netRevenue = totalIncomes - totalExpenses;
        
        model.addAttribute("transactions", txs);
        model.addAttribute("totalIncomes", totalIncomes);
        model.addAttribute("totalExpenses", totalExpenses);
        model.addAttribute("netRevenue", netRevenue);
        return "admin/finances";
    }

    @PostMapping("/finanzas/guardar")
    public String saveTransaction(@RequestParam("type") String type,
                                  @RequestParam("category") String category,
                                  @RequestParam("amount") double amount,
                                  @RequestParam("description") String description) {
        Transaction tx = Transaction.builder()
                .type(type)
                .category(category)
                .amount(amount)
                .description(description)
                .timestamp(LocalDateTime.now())
                .build();
        transactionRepository.save(tx);
        return "redirect:/admin/finanzas";
    }

    // --- SETTINGS MANAGEMENT ---
    @GetMapping("/configuracion")
    public String editSettings(Model model) {
        Settings settings = settingsRepository.findFirstByOrderByIdAsc().orElse(new Settings());
        model.addAttribute("settingsObj", settings);
        return "admin/settings";
    }

    @PostMapping("/configuracion/guardar")
    public String saveSettings(@ModelAttribute Settings settings) {
        Settings existing = settingsRepository.findFirstByOrderByIdAsc().orElse(new Settings());
        double oldTaxRate = existing.getTaxRate();
        settings.setId(existing.getId()); // preserve id
        settingsRepository.save(settings);
        
        // Propagate taxRate updates and recalculate all existing quotes
        if (oldTaxRate != settings.getTaxRate()) {
            List<Quote> quotes = quoteRepository.findAll();
            for (Quote quote : quotes) {
                quote.setTaxRate(settings.getTaxRate());
                
                double baseNetPrice = quote.getSubtotal() - quote.getDiscountAmount();
                double taxAmount = baseNetPrice * (settings.getTaxRate() / 100.0);
                quote.setTaxAmount(taxAmount);
                quote.setTotal(quote.getSubtotal() - quote.getDiscountAmount() + taxAmount);
                
                double totalCost = quote.getMaterialCost() + quote.getLaborCost() + quote.getTransportCost();
                double profit = baseNetPrice - totalCost;
                quote.setProfitAmount(profit);
                quote.setMarginPercent(baseNetPrice > 0 ? (profit * 100.0) / baseNetPrice : 0.0);
                
                quoteRepository.save(quote);
            }
        }
        return "redirect:/admin/configuracion";
    }

    // --- INVENTORY MANAGEMENT ---
    @GetMapping("/inventario")
    public String listInventory(Model model) {
        model.addAttribute("inventory", inventoryRepository.findAll());
        return "admin/inventory";
    }

    @PostMapping("/inventario/guardar")
    public String saveInventoryItem(@ModelAttribute Inventory inventory) {
        inventoryRepository.save(inventory);
        return "redirect:/admin/inventario";
    }

    // --- CRM KANBAN PIPELINE ---
    @GetMapping("/crm/pipeline")
    public String crmPipeline(Model model) {
        List<Quote> quotes = quoteRepository.findAll();
        List<String> stages = Arrays.asList("CONTACT", "ESCRIBI", "RESPONDIO", "INTERESADO", "SIN_RESPUESTA", "NEGOCIACION", "GANADO", "PERDIDO");
        Map<String, List<Quote>> pipeline = new HashMap<>();
        for (String stage : stages) {
            pipeline.put(stage, new ArrayList<>());
        }
        
        double totalValue = 0;
        int wonCount = 0;
        int lostCount = 0;
        
        for (Quote q : quotes) {
            String stage = q.getCrmStage() != null ? q.getCrmStage() : "CONTACT";
            if (pipeline.containsKey(stage)) {
                pipeline.get(stage).add(q);
            } else {
                pipeline.get("CONTACT").add(q);
            }
            totalValue += q.getTotal() > 0 ? q.getTotal() : (q.getProfitAmount() > 0 ? q.getProfitAmount() : 0);
            if ("GANADO".equals(stage) || q.getStatus() == Quote.QuoteStatus.APPROVED || q.getStatus() == Quote.QuoteStatus.COMPLETED) {
                wonCount++;
            } else if ("PERDIDO".equals(stage) || q.getStatus() == Quote.QuoteStatus.REJECTED) {
                lostCount++;
            }
        }
        
        model.addAttribute("pipeline", pipeline);
        model.addAttribute("stages", stages);
        model.addAttribute("totalOpportunities", quotes.size());
        model.addAttribute("totalValue", totalValue);
        model.addAttribute("wonCount", wonCount);
        model.addAttribute("lostCount", lostCount);
        model.addAttribute("clients", clientCardRepository.findAll());
        return "admin/crm-pipeline";
    }

    @PostMapping("/crm/pipeline/update-stage/{id}")
    public String updateCrmStage(@PathVariable String id, @RequestParam("crmStage") String crmStage) {
        Quote quote = quoteRepository.findById(id).orElseThrow();
        quote.setCrmStage(crmStage);
        quote.setUpdatedAt(LocalDateTime.now());
        quoteRepository.save(quote);
        return "redirect:/admin/crm/pipeline";
    }

    @PostMapping("/crm/oportunidad/guardar")
    public String saveOpportunity(@RequestParam("clientName") String clientName,
                                  @RequestParam(value = "clientCompany", required = false) String company,
                                  @RequestParam(value = "clientPhone", required = false) String phone,
                                  @RequestParam(value = "clientEmail", required = false) String email,
                                  @RequestParam(value = "clientAddress", required = false) String address,
                                  @RequestParam(value = "clientCity", required = false) String city,
                                  @RequestParam("serviceType") String serviceType,
                                  @RequestParam("value") double approxValue,
                                  @RequestParam("priority") String priority,
                                  @RequestParam("howMet") String howMet,
                                  @RequestParam(value = "observations", required = false) String observations) {
        Quote quote = Quote.builder()
                .quoteNumber("OP-" + LocalDateTime.now().getYear() + "-" + String.format("%04d", (quoteRepository.count() + 1)))
                .clientName(clientName)
                .clientCompany(company)
                .clientPhone(phone)
                .clientEmail(email)
                .clientAddress(address)
                .clientCity(city)
                .serviceType(serviceType)
                .total(approxValue)
                .priority(priority)
                .howMet(howMet)
                .notes(observations)
                .crmStage("CONTACT")
                .status(Quote.QuoteStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        quoteRepository.save(quote);
        return "redirect:/admin/crm/pipeline";
    }

    @PostMapping("/crm/actividad/guardar/{id}")
    public String saveCrmActivity(@PathVariable String id,
                                  @RequestParam("activityType") String type,
                                  @RequestParam("activityText") String text,
                                  @AuthenticationPrincipal UserDetails userDetails) {
        Quote quote = quoteRepository.findById(id).orElseThrow();
        
        Quote.CrmActivity activity = Quote.CrmActivity.builder()
                .id(UUID.randomUUID().toString())
                .type(type)
                .text(text)
                .timestamp(LocalDateTime.now())
                .author(userDetails.getUsername())
                .build();
                
        quote.getActivities().add(activity);
        quoteRepository.save(quote);
        return "redirect:/admin/crm/pipeline";
    }

    @GetMapping("/crm/oportunidad/eliminar/{id}")
    public String deleteOpportunity(@PathVariable String id) {
        quoteRepository.deleteById(id);
        return "redirect:/admin/crm/pipeline";
    }

    @GetMapping("/proyectos-activos")
    public String listActiveProjects(Model model) {
        model.addAttribute("projects", workProjectRepository.findAll());
        return "admin/projects-active";
    }

    @GetMapping("/proyectos-activos/nuevo")
    public String newProjectForm(Model model) {
        model.addAttribute("project", new WorkProject());
        model.addAttribute("statusList", WorkProject.ProjectStatus.values());
        model.addAttribute("clients", clientCardRepository.findAll());
        return "admin/project-active-form";
    }

    @PostMapping("/proyectos-activos/crear-manual")
    public String createManualProject(@ModelAttribute WorkProject project,
                                     @RequestParam("revenue") double revenue,
                                     @RequestParam("cost") double cost) {
        project.setProjectId("PRJ-" + LocalDateTime.now().getYear() + "-" + String.format("%04d", (workProjectRepository.count() + 1)));
        project.setStatus(WorkProject.ProjectStatus.PLANNING);
        project.setProgressPercentage(0);
        project.setBudget(new WorkProject.ProjectBudget(revenue, cost, revenue, revenue - cost));
        project.setStartDate(LocalDateTime.now());
        project.setEndDate(LocalDateTime.now().plusDays(15));
        workProjectRepository.save(project);
        return "redirect:/admin/proyectos-activos";
    }

    @GetMapping("/proyectos-activos/ver/{id}")
    public String viewActiveProject(@PathVariable String id, Model model) {
        WorkProject project = workProjectRepository.findById(id).orElseThrow();
        model.addAttribute("project", project);
        model.addAttribute("statusList", WorkProject.ProjectStatus.values());
        model.addAttribute("technicians", userRepository.findAll());
        return "admin/project-active-detail";
    }

    @PostMapping("/proyectos-activos/guardar-tarea/{id}")
    public String saveProjectTask(@PathVariable String id,
                                 @RequestParam("taskTitle") String title,
                                 @RequestParam("taskDesc") String description,
                                 @RequestParam(value = "taskAssigned", required = false) String assignedTo) {
        WorkProject project = workProjectRepository.findById(id).orElseThrow();
        
        WorkProject.ProjectTask task = WorkProject.ProjectTask.builder()
                .id(UUID.randomUUID().toString())
                .title(title)
                .description(description)
                .completed(false)
                .assignedTo(assignedTo)
                .dueDate(LocalDateTime.now().plusDays(3))
                .build();
        
        project.getTasks().add(task);
        
        // Recalculate progress percentage
        long completedCount = project.getTasks().stream().filter(WorkProject.ProjectTask::isCompleted).count();
        int totalTasks = project.getTasks().size();
        project.setProgressPercentage(totalTasks > 0 ? (int) ((completedCount * 100) / totalTasks) : 0);
        
        workProjectRepository.save(project);
        return "redirect:/admin/proyectos-activos/ver/" + id;
    }

    @GetMapping("/proyectos-activos/completar-tarea/{projectId}/{taskId}")
    public String completeProjectTask(@PathVariable String projectId, @PathVariable String taskId) {
        WorkProject project = workProjectRepository.findById(projectId).orElseThrow();
        for (WorkProject.ProjectTask t : project.getTasks()) {
            if (t.getId().equals(taskId)) {
                t.setCompleted(true);
                break;
            }
        }
        
        long completedCount = project.getTasks().stream().filter(WorkProject.ProjectTask::isCompleted).count();
        int totalTasks = project.getTasks().size();
        project.setProgressPercentage(totalTasks > 0 ? (int) ((completedCount * 100) / totalTasks) : 0);
        
        workProjectRepository.save(project);
        return "redirect:/admin/proyectos-activos/ver/" + projectId;
    }
}
