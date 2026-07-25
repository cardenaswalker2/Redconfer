package com.redconfer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "services")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Service {
    @Id
    private String id;
    private String name;
    private String slug; // Friendly URL
    private String category; // CCTV, Network, Automation, Security, etc.
    private String shortDescription;
    private String fullDescription;
    
    private double priceFrom;
    private boolean promotion;
    private double promoPrice;
    private boolean featured;
    private boolean active = true;
    
    private List<String> benefits;
    private List<String> features;
    private String estimatedTime;
    private String warranty;
    
    // Paths to images/videos
    private List<String> images;
    private String videoUrl;
}
