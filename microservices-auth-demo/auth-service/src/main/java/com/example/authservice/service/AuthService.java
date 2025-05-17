package com.example.authservice.service;

import com.example.authservice.model.*;
import com.example.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    private static final Map<String, List<String>> SERVICE_ROLE_PREFIXES = Map.of(
        "chat", List.of("ROLE_CHAT_"),
        "hrms", List.of("ROLE_HRMS_")
    );

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", user.getRoles());
        
        String jwtToken = jwtService.generateToken(claims, user);
        
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles())
                .build();
    }

    public AuthenticationResponse register(RegistrationRequest request) {
        // Default role if none provided
        if (request.getRoles() == null || request.getRoles().isEmpty()) {
            request.setRoles(Collections.singleton(Role.ROLE_USER));
        }
        
        // Create user entity
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(request.getRoles())
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();
        
        // Save user
        userRepository.save(user);
        
        // Generate JWT token
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", user.getRoles());
        
        String jwtToken = jwtService.generateToken(claims, user);
        
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles())
                .build();
    }
    
    public Set<Role> getUserRoles(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return user.getRoles();
    }
    
    public AuthenticationResponse updateUserRoles(String username, Set<Role> roles) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        
        user.setRoles(roles);
        userRepository.save(user);
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", user.getRoles());
        
        String jwtToken = jwtService.generateToken(claims, user);
        
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles())
                .build();
    }
    
    public AuthenticationResponse registerWithServiceRoles(ServiceRoleRegistrationRequest request) {
        // Convert the service roles map to a flat set of Role enum values
        Set<Role> allRoles = new HashSet<>();
        
        // Always add the basic user role
        allRoles.add(Role.ROLE_USER);
        
        // Add service-specific roles
        if (request.getServiceRoles() != null) {
            request.getServiceRoles().forEach((service, roles) -> {
                allRoles.addAll(roles);
            });
        }
        
        // Create user entity
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(allRoles)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();
        
        // Save user
        userRepository.save(user);
        
        // Generate JWT token
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", user.getRoles());
        
        // Add service-specific role claims
        if (request.getServiceRoles() != null) {
            request.getServiceRoles().forEach((service, roles) -> {
                claims.put(service + "_roles", roles);
            });
        }
        
        String jwtToken = jwtService.generateToken(claims, user);
        
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles())
                .build();
    }
    
    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }
    
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }
} 