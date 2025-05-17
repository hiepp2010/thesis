package com.example.authservice.controller;

import com.example.authservice.model.*;
import com.example.authservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("Auth service is up and running!");
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody RegistrationRequest request) {
        if (authService.usernameExists(request.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        
        if (authService.emailExists(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }
    
    @GetMapping("/check-username/{username}")
    public ResponseEntity<Boolean> checkUsernameExists(@PathVariable String username) {
        return ResponseEntity.ok(authService.usernameExists(username));
    }
    
    @GetMapping("/check-email/{email}")
    public ResponseEntity<Boolean> checkEmailExists(@PathVariable String email) {
        return ResponseEntity.ok(authService.emailExists(email));
    }
    
    @GetMapping("/user/{username}/roles")
    public ResponseEntity<Set<Role>> getUserRoles(@PathVariable String username) {
        return ResponseEntity.ok(authService.getUserRoles(username));
    }
    
    @PostMapping("/user/{username}/roles")
    public ResponseEntity<AuthenticationResponse> updateUserRoles(
            @PathVariable String username,
            @RequestBody RoleUpdateRequest request) {
        return ResponseEntity.ok(authService.updateUserRoles(username, request.getRoles()));
    }
    
    @PostMapping("/register-with-service-roles")
    public ResponseEntity<AuthenticationResponse> registerWithServiceRoles(@RequestBody ServiceRoleRegistrationRequest request) {
        if (authService.usernameExists(request.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        
        if (authService.emailExists(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        
        return ResponseEntity.ok(authService.registerWithServiceRoles(request));
    }
} 