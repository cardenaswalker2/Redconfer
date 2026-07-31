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

        // Clean up any corrupt services with empty ID in MongoDB
        if (serviceRepository.existsById("")) {
            serviceRepository.deleteById("");
            System.out.println("Cleaned up corrupt service with empty ID.");
        }

        // 3. Initialize Default Services
        if (serviceRepository.count() == 0) {
            List<Service> defaultServices = Arrays.asList(
                Service.builder()
                    .name("Cámaras de Seguridad (CCTV)")
                    .slug("camaras-de-seguridad-cctv")
                    .category("Videovigilancia")
                    .shortDescription("Instalación y mantenimiento de sistemas IP, analógicos y térmicos. Monitoreo remoto en tiempo real, analíticas de video con inteligencia artificial.")
                    .fullDescription("Diseño e instalación de sistemas de circuito cerrado de televisión. Analítica inteligente, cámaras térmicas y de alta definición.")
                    .priceFrom(450000)
                    .featured(true)
                    .active(true)
                    .icon("bi-camera-video-fill")
                    .displayOrder(1)
                    .build(),
                Service.builder()
                    .name("Redes y Cableado Estructurado")
                    .slug("redes-y-cableado-estructurado")
                    .category("Conectividad")
                    .shortDescription("Certificación de redes en Cat6, Cat6A y Cat7. Fusionado y tendido de Fibra Óptica aérea o subterránea. Ordenamiento de racks.")
                    .fullDescription("Cableado estructurado de datos, voz y video. Certificación de puntos de red, diseño e implementación de cuartos de telecomunicaciones.")
                    .priceFrom(350000)
                    .featured(true)
                    .active(true)
                    .icon("bi-diagram-3-fill")
                    .displayOrder(2)
                    .build(),
                Service.builder()
                    .name("Control de Acceso y Biometría")
                    .slug("control-de-acceso-y-biometria")
                    .category("Seguridad")
                    .shortDescription("Sistemas de control peatonal y vehicular. Cerraduras electromagnéticas, biometría facial, lectores de tarjetas RFID y barreras.")
                    .fullDescription("Control de ingreso y salida peatonal y vehicular. Reconocimiento facial, huella dactilar, tarjetas de proximidad y control de tiempo.")
                    .priceFrom(600000)
                    .featured(true)
                    .active(true)
                    .icon("bi-fingerprint")
                    .displayOrder(3)
                    .build(),
                Service.builder()
                    .name("Alarmas y Cercas Eléctricas")
                    .slug("alarmas-y-cercas-electricas")
                    .category("Seguridad")
                    .shortDescription("Protección perimetral con alarmas contra robo inteligentes conectadas a su móvil y cercos eléctricos de alto voltaje controlado homologados.")
                    .fullDescription("Protección integral para residencias y comercios. Alarmas cableadas e inalámbricas monitoreadas en tiempo real desde aplicación móvil.")
                    .priceFrom(800000)
                    .featured(true)
                    .active(true)
                    .icon("bi-shield-lock-fill")
                    .displayOrder(4)
                    .build(),
                Service.builder()
                    .name("Paneles y Energía Solar")
                    .slug("paneles-y-energia-solar")
                    .category("Energía")
                    .shortDescription("Estudios de ahorro energético. Instalaciones fotovoltaicas híbridas, autónomas y conectadas a la red de alta eficiencia para empresas y residencias.")
                    .fullDescription("Diseño e instalación de sistemas solares fotovoltaicos. Proyectos conectados a la red y sistemas con almacenamiento para respaldo de energía.")
                    .priceFrom(4500000)
                    .featured(true)
                    .active(true)
                    .icon("bi-sun-fill")
                    .displayOrder(5)
                    .build(),
                Service.builder()
                    .name("Automatización y Domótica")
                    .slug("automatizacion-y-domotica")
                    .category("Automatización")
                    .shortDescription("Control inteligente de iluminación, portones automatizados, aires acondicionados y sistemas de audio envolventes integrados en una App.")
                    .fullDescription("Transforme su hogar u oficina en un espacio inteligente. Control total de luces, clima, seguridad y entretenimiento desde su smartphone.")
                    .priceFrom(1200000)
                    .featured(true)
                    .active(true)
                    .icon("bi-cpu")
                    .displayOrder(6)
                    .build()
            );
            serviceRepository.saveAll(defaultServices);
        }
    }
}

