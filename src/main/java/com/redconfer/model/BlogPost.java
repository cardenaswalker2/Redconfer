package com.redconfer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "blog_posts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlogPost {
    @Id
    private String id;
    private String title;
    private String slug;
    private String summary;
    private String content; // Rich Text/HTML content
    private String author;
    private String bannerImage;
    private List<String> tags;
    
    // SEO Fields
    private String metaTitle;
    private String metaDescription;
    private String keywords;
    
    private boolean published = true;
    
    @CreatedDate
    private LocalDateTime createdAt;
    private LocalDateTime publishedAt;
}
