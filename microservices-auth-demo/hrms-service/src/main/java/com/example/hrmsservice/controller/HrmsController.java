package com.example.hrmsservice.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/hrms")
public class HrmsController {

    @GetMapping("/public/status")
    public Map<String, String> getPublicStatus() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "HRMS Service is up and running");
        response.put("visibility", "public");
        return response;
    }

    @GetMapping("/private/status")
    public Map<String, Object> getPrivateStatus() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> response = new HashMap<>();
        response.put("status", "HRMS Service is up and running");
        response.put("visibility", "private");
        response.put("user", auth.getName());
        response.put("authorities", auth.getAuthorities());
        return response;
    }

    @GetMapping("/private/employees")
    public Map<String, Object> getEmployees() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Map<String, Object> response = new HashMap<>();
        response.put("user", auth.getName());
        response.put("message", "Access to employee data");
        return response;
    }
} 