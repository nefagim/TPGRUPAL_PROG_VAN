package com.empresa.demostockapp.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.Set;

public class UserUpdateRequestDTO {

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Email should be valid")
    @Size(max = 50, message = "Email cannot exceed 50 characters")
    private String email;

    // Optional: Add password update field if needed, with appropriate handling.
    // For this DTO, we are only allowing email and roles update.

    @NotEmpty(message = "Roles cannot be empty, provide at least one role e.g. ROLE_USER")
    private Set<String> roles; // Example: {"ROLE_USER", "ROLE_MODERATOR"}

    public UserUpdateRequestDTO() {
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
}
