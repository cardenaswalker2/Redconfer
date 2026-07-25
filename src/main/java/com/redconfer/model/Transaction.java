package com.redconfer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "transactions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    @Id
    private String id;
    private String type; // INCOME, EXPENSE
    private String category; // GASOLINE, TOLLS, MATERIALS, TOOLS, SERVICES, SALARY, REVENUE, OTHER
    private double amount;
    private String description;
    private String projectId; // optional link to a WorkProject
    
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
