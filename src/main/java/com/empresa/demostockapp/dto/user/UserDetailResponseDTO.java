package com.empresa.demostockapp.dto.user;

import com.empresa.demostockapp.model.User;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class UserDetailResponseDTO {

    private Long id;
    private String username;
    private String email;
    private List<String> roles;

    public UserDetailResponseDTO() {
    }

    public UserDetailResponseDTO(Long id, String username, String email, List<String> roles) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
    }

    public static UserDetailResponseDTO fromUser(User user) {
        List<String> roleList = Arrays.stream(user.getRoles().split(","))
                .map(String::trim)
                .filter(role -> !role.isEmpty())
                .collect(Collectors.toList());
        return new UserDetailResponseDTO(user.getId(), user.getUsername(), user.getEmail(), roleList);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
