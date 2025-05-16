package com.example.hrmsservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class HrmsServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(HrmsServiceApplication.class, args);
    }
} 