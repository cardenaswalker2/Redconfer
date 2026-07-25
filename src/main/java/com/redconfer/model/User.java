package com.redconfer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String username; // will be email
    private String password;
    
    private String name;
    private String company;
    private String phone;
    private String whatsapp;
    private String address;
    private String city;
    private String department;
    
    private UserRole role;
    private boolean active = true;
    
    private String privateNotes;
    
    @CreatedDate
    private LocalDateTime createdAt;
}
