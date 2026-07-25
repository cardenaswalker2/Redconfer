package com.redconfer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Document(collection = "settings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Settings {
    @Id
    private String id;
    
    // Identity visual
    private String siteName;
    private String logoUrl;
    private String primaryColor; // e.g. #C61A22 (Redconfer Red)
    private String darkColor;    // e.g. #373F47
    
    // Financial settings
    private double taxRate; // e.g. 19.0
    private String currency; // e.g. COP, USD
    private String currencySymbol; // e.g. $
    
    // Contact Info
    private String phone;
    private String whatsapp;
    private String email;
    private String address;
    private String schedule;
    
    // Social links
    private String facebook;
    private String instagram;
    private String linkedin;
    private String twitter;
    
    // Meta / SEO Defaults
    private String metaTitle;
    private String metaDescription;
    private String robotsTxt;
    
    // Dynamic website home content
    private Map<String, String> homeTexts;
}
