package com.redconfer.controller;

import com.redconfer.model.Quote;
import com.redconfer.model.Ticket;
import com.redconfer.model.User;
import com.redconfer.repository.QuoteRepository;
import com.redconfer.repository.SettingsRepository;
import com.redconfer.repository.TicketRepository;
import com.redconfer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/cliente")
@RequiredArgsConstructor
public class ClientController {

    private final UserRepository userRepository;
    private final QuoteRepository quoteRepository;
    private final TicketRepository ticketRepository;
    private final SettingsRepository settingsRepository;

    @ModelAttribute
    public void addClientAttributes(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            User user = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
            model.addAttribute("clientUser", user);
        }
        model.addAttribute("settings", settingsRepository.findFirstByOrderByIdAsc().orElse(null));
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        List<Quote> quotes = quoteRepository.findByClientId(user.getId());
        List<Ticket> tickets = ticketRepository.findByClientId(user.getId());

        model.addAttribute("quotes", quotes);
        model.addAttribute("tickets", tickets);
        return "client/dashboard";
    }

    @GetMapping("/cotizaciones/ver/{id}")
    public String viewQuote(@PathVariable String id, Model model) {
        Quote quote = quoteRepository.findById(id).orElseThrow();
        model.addAttribute("quote", quote);
        return "client/quote-detail";
    }

    // --- SUPPORT TICKETS ---
    @GetMapping("/tickets")
    public String listTickets(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        model.addAttribute("tickets", ticketRepository.findByClientId(user.getId()));
        return "client/tickets";
    }

    @GetMapping("/tickets/nuevo")
    public String newTicketForm() {
        return "client/ticket-form";
    }

    @PostMapping("/tickets/guardar")
    public String saveTicket(@AuthenticationPrincipal UserDetails userDetails,
                             @RequestParam("subject") String subject,
                             @RequestParam("category") String category,
                             @RequestParam("priority") Ticket.TicketPriority priority,
                             @RequestParam("description") String description) {
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        
        Ticket ticket = Ticket.builder()
                .ticketNumber("TK-" + String.format("%04d", (ticketRepository.count() + 1)))
                .clientId(user.getId())
                .clientName(user.getName())
                .subject(subject)
                .category(category)
                .priority(priority)
                .description(description)
                .status(Ticket.TicketStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
                
        ticketRepository.save(ticket);
        return "redirect:/cliente/tickets";
    }

    @GetMapping("/tickets/ver/{id}")
    public String viewTicket(@PathVariable String id, Model model) {
        Ticket ticket = ticketRepository.findById(id).orElseThrow();
        model.addAttribute("ticket", ticket);
        return "client/ticket-detail";
    }

    @PostMapping("/tickets/responder/{id}")
    public String replyTicket(@PathVariable String id,
                              @AuthenticationPrincipal UserDetails userDetails,
                              @RequestParam("message") String message) {
        Ticket ticket = ticketRepository.findById(id).orElseThrow();
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();

        Ticket.TicketMessage msg = new Ticket.TicketMessage(
                user.getName(),
                "Client",
                message,
                LocalDateTime.now()
        );
        ticket.getMessages().add(msg);
        ticket.setUpdatedAt(LocalDateTime.now());
        ticketRepository.save(ticket);
        
        return "redirect:/cliente/tickets/ver/" + id;
    }
}
