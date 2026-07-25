package com.redconfer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "work_projects")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkProject {
    @Id
    private String id;
    private String projectId; // e.g., PRJ-2026-0001
    private String quoteId;   // Reference to original Quote
    private String name;
    private String clientId;
    private String clientName;
    private String description;
    
    @Builder.Default
    private ProjectStatus status = ProjectStatus.PLANNING;
    
    private int progressPercentage;
    
    private ProjectBudget budget;
    
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    
    @Builder.Default
    private List<String> assignedTechnicians = new ArrayList<>(); // User IDs
    
    @Builder.Default
    private List<ProjectTask> tasks = new ArrayList<>();
    
    @Builder.Default
    private List<ProjectDocument> documents = new ArrayList<>();
    
    @Builder.Default
    private List<String> comments = new ArrayList<>();
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;

    public enum ProjectStatus {
        PLANNING, IN_PROGRESS, ON_HOLD, COMPLETED, CANCELLED
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectBudget {
        private double estimatedCost;
        private double actualCost;
        private double revenue;
        private double margin; // (revenue - actualCost)
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProjectTask {
        private String id;
        private String title;
        private String description;
        private boolean completed;
        private LocalDateTime dueDate;
        private String assignedTo; // User ID
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProjectDocument {
        private String name;
        private String fileUrl;
        private String fileType; // Blueprint, Contract, Technical Manual, Photos
        private LocalDateTime uploadedAt;
    }
}
