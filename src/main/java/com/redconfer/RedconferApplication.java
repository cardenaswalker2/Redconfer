package com.redconfer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class RedconferApplication {
    public static void main(String[] args) {
        SpringApplication.run(RedconferApplication.class, args);
    }
}
