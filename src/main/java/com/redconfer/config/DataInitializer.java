package com.redconfer.config;

import com.redconfer.model.*;
import com.redconfer.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;
    private final SettingsRepository settingsRepository;
    private final ProjectRepository projectRepository;
    private final PasswordEncoder passwordEncoder;
    private final ClientCardRepository clientCardRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public void run(String... args) throws Exception {
        // 1. Initialize Settings
        if (settingsRepository.count() == 0) {
            Map<String, String> homeTexts = new HashMap<>();
            homeTexts.put("heroTitle", "Soluciones Tecnológicas e Infraestructura de Seguridad de Alto Nivel");
            homeTexts.put("heroSubtitle", "REDCONFER Comprehensive Services ofrece integración de cámaras, cableado estructurado, control de acceso y fibra óptica para empresas y residencias.");
            homeTexts.put("aboutUs", "Somos una compañía especializada en el diseño, implementación y mantenimiento de sistemas tecnológicos avanzados. Proporcionamos seguridad integral y conectividad de máxima velocidad.");
            
            Settings defaultSettings = Settings.builder()
                    .siteName("REDCONFER")
                    .logoUrl("/images/logo.png")
                    .primaryColor("#C61A22")
                    .darkColor("#373F47")
                    .taxRate(19.0)
                    .currency("COP")
                    .currencySymbol("$")
                    .phone("+57 323 357 0996")
                    .whatsapp("573233570996")
                    .email("contacto@redconfer.com")
                    .address("Calle 100 #15-30, Cartagena, Colombia")
                    .schedule("Lunes a Viernes: 8:00 AM - 6:00 PM | Sábado: 8:00 AM - 1:00 PM")
                    .facebook("https://facebook.com/redconfer")
                    .instagram("https://instagram.com/redconfer")
                    .metaTitle("REDCONFER - Servicios Tecnológicos e Integración de Seguridad")
                    .metaDescription("Cámaras de seguridad, Alarmas, Cableado Estructurado, Fibra Óptica y Control de Acceso Profesional.")
                    .homeTexts(homeTexts)
                    .build();
            settingsRepository.save(defaultSettings);
        }

        // 2. Initialize Admin User
        if (userRepository.count() == 0) {
            User admin = User.builder()
                    .username("admin@redconfer.com")
                    .password(passwordEncoder.encode("admin123"))
                    .name("Administrador REDCONFER")
                    .role(UserRole.ROLE_ADMIN)
                    .active(true)
                    .createdAt(LocalDateTime.now())
                    .build();
            userRepository.save(admin);
        }
    }
}
