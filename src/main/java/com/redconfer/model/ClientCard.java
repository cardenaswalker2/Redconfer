package com.redconfer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "client_cards")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientCard {
    @Id
    private String id;
    private String nit;
    private String name;
    private String company;
    private String address;
    private String city;
    private String department;
    private String gps;
    private String privateNotes;
    
    @Builder.Default
    private List<String> emails = new ArrayList<>();
    
    @Builder.Default
    private List<String> phones = new ArrayList<>();
    
    @Builder.Default
    private List<String> tags = new ArrayList<>(); // VIP, Frecuente, Potencial, Inactivo, Perdido
    
    private String origin; // WhatsApp, Facebook, Instagram, Web, Llamada, Referido, Presencial
    
    @Builder.Default
    private List<TimelineEntry> timeline = new ArrayList<>();
    
    @Builder.Default
    private List<InstalledEquipment> installedEquipment = new ArrayList<>();
    
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimelineEntry {
        private String id;
        private String text;
        private LocalDateTime timestamp;
        private String author;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InstalledEquipment {
        private String id;
        private String name; // e.g. Hikvision PTZ Dome
        private String serialNumber;
        private String location;
        private LocalDateTime installDate;
        private String warrantyPeriod; // e.g. 1 year
    }
}
