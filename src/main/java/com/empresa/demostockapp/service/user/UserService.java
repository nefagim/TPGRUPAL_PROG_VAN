package com.empresa.demostockapp.service.user;

import com.empresa.demostockapp.dto.user.UserCreateRequestDTO;
import com.empresa.demostockapp.dto.user.UserDetailResponseDTO;
import com.empresa.demostockapp.dto.user.UserUpdateRequestDTO;
import com.empresa.demostockapp.exception.ResourceNotFoundException;
import com.empresa.demostockapp.model.User;
import com.empresa.demostockapp.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserDetailResponseDTO> findAllUsers() {
        return userRepository.findAll().stream()
                .map(UserDetailResponseDTO::fromUser)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserDetailResponseDTO findUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return UserDetailResponseDTO.fromUser(user);
    }

    @Transactional
    public UserDetailResponseDTO createUser(UserCreateRequestDTO userCreateRequestDTO) {
        if (userRepository.existsByUsername(userCreateRequestDTO.getUsername())) {
            throw new IllegalArgumentException("Error: Username is already taken!");
        }

        if (userRepository.existsByEmail(userCreateRequestDTO.getEmail().toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Error: Email is already in use!");
        }

        User user = new User();
        user.setUsername(userCreateRequestDTO.getUsername());
        user.setEmail(userCreateRequestDTO.getEmail().toLowerCase(Locale.ROOT));
        user.setPassword(passwordEncoder.encode(userCreateRequestDTO.getPassword()));
        user.setRoles(convertRolesSetToString(userCreateRequestDTO.getRoles()));

        User savedUser = userRepository.save(user);
        return UserDetailResponseDTO.fromUser(savedUser);
    }

    @Transactional
    public UserDetailResponseDTO updateUser(Long id, UserUpdateRequestDTO userUpdateRequestDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        String newEmail = userUpdateRequestDTO.getEmail().toLowerCase(Locale.ROOT);
        if (!user.getEmail().equals(newEmail) && userRepository.existsByEmail(newEmail)) {
            throw new IllegalArgumentException("Error: Email is already in use by another account!");
        }
        user.setEmail(newEmail);
        user.setRoles(convertRolesSetToString(userUpdateRequestDTO.getRoles()));

        User updatedUser = userRepository.save(user);
        return UserDetailResponseDTO.fromUser(updatedUser);
    }

    @Transactional
    public void deleteUserById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        // Consider implications: what if user is associated with other entities?
        // For now, direct deletion. Add checks or soft delete if needed later.
        // Also, prevent self-deletion if the currently authenticated user is this one.
        // (This check would typically be in the controller or a higher service layer).
        userRepository.deleteById(id);
    }

    private String convertRolesSetToString(Set<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return "ROLE_USER"; // Default role if none provided
        }
        return roles.stream()
                .map(role -> role.toUpperCase().startsWith("ROLE_") ? role.toUpperCase() : "ROLE_" + role.toUpperCase())
                .collect(Collectors.joining(","));
    }
}
