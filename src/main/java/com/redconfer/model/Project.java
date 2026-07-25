package com.redconfer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "projects")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Project {
    @Id
    private String id;
    private String title;
    private String clientName;
    private String category; // CCTV, Fibra Optica, Control Acceso, etc.
    private String description;
    private String zone; // e.g. Cartagena, Turbaco, etc. (for interactive project map)
    
    // Portfolio images
    private String imageBefore; // Path/URL
    private String imageAfter;  // Path/URL
    private List<String> gallery; // Other pictures
    private String videoUrl;
    
    private boolean active = true;
}
