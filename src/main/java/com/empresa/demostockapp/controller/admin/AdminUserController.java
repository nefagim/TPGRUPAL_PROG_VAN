package com.empresa.demostockapp.controller.admin;

import com.empresa.demostockapp.dto.user.UserCreateRequestDTO;
import com.empresa.demostockapp.dto.user.UserDetailResponseDTO;
import com.empresa.demostockapp.dto.user.UserUpdateRequestDTO;
import com.empresa.demostockapp.service.user.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')") // Class-level authorization for ADMIN role
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserDetailResponseDTO>> getAllUsers() {
        List<UserDetailResponseDTO> users = userService.findAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDetailResponseDTO> getUserById(@PathVariable Long id) {
        UserDetailResponseDTO user = userService.findUserById(id);
        return ResponseEntity.ok(user);
    }

    @PostMapping
    public ResponseEntity<UserDetailResponseDTO> createUser(@Valid @RequestBody UserCreateRequestDTO userCreateRequestDTO) {
        UserDetailResponseDTO createdUser = userService.createUser(userCreateRequestDTO);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDetailResponseDTO> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequestDTO userUpdateRequestDTO) {
        UserDetailResponseDTO updatedUser = userService.updateUser(id, userUpdateRequestDTO);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }
}
