package com.redconfer.controller;

import com.redconfer.model.BlogPost;
import com.redconfer.model.Project;
import com.redconfer.model.Quote;
import com.redconfer.model.Service;
import com.redconfer.repository.BlogPostRepository;
import com.redconfer.repository.ProjectRepository;
import com.redconfer.repository.ServiceRepository;
import com.redconfer.repository.SettingsRepository;
import com.redconfer.service.QuoteService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class PublicController {

    private final ServiceRepository serviceRepository;
    private final ProjectRepository projectRepository;
    private final BlogPostRepository blogPostRepository;
    private final SettingsRepository settingsRepository;
    private final QuoteService quoteService;

    private static final String UPLOAD_DIR = "src/main/resources/static/uploads/";

    @ModelAttribute
    public void addGlobalAttributes(Model model) {
        model.addAttribute("settings", settingsRepository.findFirstByOrderByIdAsc().orElse(null));
        model.addAttribute("allServices", serviceRepository.findByActiveTrue());
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("featuredServices", serviceRepository.findByFeaturedTrueAndActiveTrue());
        model.addAttribute("projects", projectRepository.findByActiveTrue());
        model.addAttribute("posts", blogPostRepository.findByPublishedTrueOrderByPublishedAtDesc());
        return "index";
    }

    @GetMapping("/servicios/{slug}")
    public String serviceDetail(@PathVariable String slug, Model model) {
        Service service = serviceRepository.findBySlug(slug)
                .orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado"));
        model.addAttribute("service", service);
        return "service-detail";
    }

    @GetMapping("/cotizador")
    public String quoteWizard(@RequestParam(required = false) Boolean success, Model model) {
        if (Boolean.TRUE.equals(success)) {
            model.addAttribute("successMessage", "¡Tu solicitud de cotización ha sido registrada exitosamente! Un ingeniero revisará la información e imágenes adjuntas.");
            return "quote-success";
        }
        model.addAttribute("quote", new Quote());
        return "quote-wizard";
    }

    @PostMapping("/cotizador")
    public String submitQuote(@ModelAttribute Quote quote,
                              @RequestParam("photos") MultipartFile[] files,
                              Model model) {
        try {
            List<String> uploadedPaths = new ArrayList<>();
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                    Path filePath = uploadPath.resolve(filename);
                    Files.copy(file.getInputStream(), filePath);
                    uploadedPaths.add("/uploads/" + filename);
                }
            }
            quote.setClientPhotos(uploadedPaths);
            quoteService.createQuoteRequest(quote);
            model.addAttribute("successMessage", "¡Tu solicitud de cotización ha sido enviada exitosamente! Revisaremos la información y te contactaremos por WhatsApp.");
        } catch (IOException e) {
            model.addAttribute("errorMessage", "Error al cargar archivos. Inténtelo de nuevo.");
            return "quote-wizard";
        }
        return "quote-success";
    }

    @GetMapping("/cobertura")
    public String coverageCalc() {
        return "coverage-calc";
    }

    @GetMapping("/proyectos")
    public String portfolio(Model model) {
        model.addAttribute("projects", projectRepository.findByActiveTrue());
        return "portfolio";
    }

    @GetMapping("/blog")
    public String blog(Model model) {
        model.addAttribute("posts", blogPostRepository.findByPublishedTrueOrderByPublishedAtDesc());
        return "blog";
    }

    @GetMapping("/blog/{slug}")
    public String blogDetail(@PathVariable String slug, Model model) {
        BlogPost post = blogPostRepository.findBySlug(slug)
                .orElseThrow(() -> new IllegalArgumentException("Artículo no encontrado"));
        model.addAttribute("post", post);
        return "blog-detail";
    }

    @GetMapping("/login")
    public String login(HttpServletRequest request) {
        request.getSession(true);
        return "login";
    }

    @GetMapping("/dashboard-redirect")
    public String dashboardRedirect(Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String role = authority.getAuthority();
            if (role.equals("ROLE_ADMIN") || role.equals("ROLE_EMPLOYEE")) {
                return "redirect:/admin/dashboard";
            } else if (role.equals("ROLE_CLIENT")) {
                return "redirect:/cliente/dashboard";
            }
        }
        return "redirect:/";
    }
}
