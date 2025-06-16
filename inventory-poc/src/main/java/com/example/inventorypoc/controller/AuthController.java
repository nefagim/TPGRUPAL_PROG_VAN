package com.example.inventorypoc.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager; // For managing users
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Placeholder for User DTO for registration
class UserRegistrationDto {
    private String username;
    private String password;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // Autowire UserDetailsManager if we were to actually add users to InMemoryUserDetailsManager
    // For this PoC, we'll keep it simpler as direct modification of InMemoryUserDetailsManager
    // post-startup can be tricky. We have pre-defined users.

    @Autowired
    private UserDetailsManager userDetailsManager; // This would be InMemoryUserDetailsManager

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody UserRegistrationDto registrationDto) {
        // Basic validation
        if (registrationDto.getUsername() == null || registrationDto.getUsername().isEmpty() ||
            registrationDto.getPassword() == null || registrationDto.getPassword().isEmpty()) {
            return ResponseEntity.badRequest().body("Username and password are required.");
        }

        // Check if user already exists (UserDetailsManager can do this)
        if (userDetailsManager.userExists(registrationDto.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists.");
        }

        UserDetails newUser = User.builder()
            .username(registrationDto.getUsername())
            .password(passwordEncoder.encode(registrationDto.getPassword()))
            .roles("USER") // Default role
            .build();

        userDetailsManager.createUser(newUser); // Add user to the manager

        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully: " + registrationDto.getUsername());
    }

    // A login endpoint is not strictly required for HTTP Basic or session-based auth
    // as Spring Security handles it. If implementing token-based auth (e.g., JWT),
    // a /login endpoint would typically be used to issue a token upon successful authentication.
    // For this PoC, HTTP Basic is sufficient.
}
