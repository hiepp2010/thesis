package com.example.authservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRoleRegistrationRequest {
    private String username;
    private String password;
    private String email;
    
    // Map of service names to roles
    // Example: {"chat": ["ROLE_CHAT_USER"], "hrms": ["ROLE_HRMS_ADMIN"]}
    private Map<String, Set<Role>> serviceRoles;
} 