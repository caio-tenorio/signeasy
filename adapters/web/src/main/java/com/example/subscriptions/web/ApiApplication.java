package com.example.subscriptions.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.example.subscriptions")
@EnableJpaRepositories(basePackages = "com.example.subscriptions.persistence.jpa")
@EntityScan(basePackages = "com.example.subscriptions.domain.model")
public class ApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiApplication.class, args);
    }
}