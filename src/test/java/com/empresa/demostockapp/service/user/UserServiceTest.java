package com.empresa.demostockapp.service.user;

import com.empresa.demostockapp.dto.user.UserCreateRequestDTO;
import com.empresa.demostockapp.dto.user.UserDetailResponseDTO;
import com.empresa.demostockapp.dto.user.UserUpdateRequestDTO;
import com.empresa.demostockapp.exception.ResourceNotFoundException;
import com.empresa.demostockapp.model.User;
import com.empresa.demostockapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user1, user2;
    private UserCreateRequestDTO createDTO;
    private UserUpdateRequestDTO updateDTO;

    @BeforeEach
    void setUp() {
        user1 = new User("user1", "encodedPassword1", "user1@example.com", "ROLE_USER");
        user1.setId(1L);

        user2 = new User("user2", "encodedPassword2", "user2@example.com", "ROLE_USER,ROLE_ADMIN");
        user2.setId(2L);

        createDTO = new UserCreateRequestDTO();
        createDTO.setUsername("newUser");
        createDTO.setEmail("newuser@example.com");
        createDTO.setPassword("password123");
        createDTO.setRoles(Set.of("USER", "MANAGER")); // Raw roles

        updateDTO = new UserUpdateRequestDTO();
        updateDTO.setEmail("updateduser1@example.com");
        updateDTO.setRoles(Set.of("ADMIN")); // Raw roles
    }

    @Test
    void findAllUsers_success() {
        when(userRepository.findAll()).thenReturn(List.of(user1, user2));
        List<UserDetailResponseDTO> result = userService.findAllUsers();
        assertEquals(2, result.size());
        assertEquals("user1", result.get(0).getUsername());
        assertEquals("user2", result.get(1).getUsername());
        assertTrue(result.get(1).getRoles().contains("ROLE_ADMIN"));
    }

    @Test
    void findUserById_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        UserDetailResponseDTO result = userService.findUserById(1L);
        assertEquals("user1", result.getUsername());
    }

    @Test
    void findUserById_notFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.findUserById(1L));
    }

    @Test
    void createUser_success() {
        when(userRepository.existsByUsername(createDTO.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(createDTO.getEmail().toLowerCase(Locale.ROOT))).thenReturn(false);
        when(passwordEncoder.encode(createDTO.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(3L); // Simulate ID assignment
            return savedUser;
        });

        UserDetailResponseDTO result = userService.createUser(createDTO);

        assertEquals(createDTO.getUsername(), result.getUsername());
        assertEquals(createDTO.getEmail().toLowerCase(Locale.ROOT), result.getEmail());
        assertTrue(result.getRoles().contains("ROLE_USER"));
        assertTrue(result.getRoles().contains("ROLE_MANAGER"));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("encodedPassword", userCaptor.getValue().getPassword());
        assertTrue(userCaptor.getValue().getRoles().contains("ROLE_USER"));
        assertTrue(userCaptor.getValue().getRoles().contains("ROLE_MANAGER"));
    }

    @Test
    void createUser_success_defaultRole() {
        createDTO.setRoles(Collections.emptySet()); // Empty set
        when(userRepository.existsByUsername(createDTO.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(createDTO.getEmail().toLowerCase(Locale.ROOT))).thenReturn(false);
        when(passwordEncoder.encode(createDTO.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(3L);
            return savedUser;
        });

        UserDetailResponseDTO result = userService.createUser(createDTO);
        assertTrue(result.getRoles().contains("ROLE_USER"));
        assertEquals(1, result.getRoles().size());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("ROLE_USER", userCaptor.getValue().getRoles());
    }


    @Test
    void createUser_usernameConflict() {
        when(userRepository.existsByUsername(createDTO.getUsername())).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(createDTO));
    }

    @Test
    void createUser_emailConflict() {
        when(userRepository.existsByUsername(createDTO.getUsername())).thenReturn(false);
        when(userRepository.existsByEmail(createDTO.getEmail().toLowerCase(Locale.ROOT))).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(createDTO));
    }

    @Test
    void updateUser_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        // Assume new email is unique
        when(userRepository.existsByEmail(updateDTO.getEmail().toLowerCase(Locale.ROOT))).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user1); // Return the same user instance for simplicity

        UserDetailResponseDTO result = userService.updateUser(1L, updateDTO);

        assertEquals(updateDTO.getEmail().toLowerCase(Locale.ROOT), result.getEmail());
        assertTrue(result.getRoles().contains("ROLE_ADMIN"));
        assertEquals(1, result.getRoles().size()); // Should only have ROLE_ADMIN now

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals("ROLE_ADMIN", userCaptor.getValue().getRoles());
    }

    @Test
    void updateUser_success_sameEmail() {
        updateDTO.setEmail(user1.getEmail()); // Setting email to the same as current
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        // existsByEmail should not be called if email is not changed
        when(userRepository.save(any(User.class))).thenReturn(user1);

        UserDetailResponseDTO result = userService.updateUser(1L, updateDTO);

        assertEquals(user1.getEmail(), result.getEmail());
        assertTrue(result.getRoles().contains("ROLE_ADMIN"));
        verify(userRepository, never()).existsByEmail(anyString()); // Not called if email doesn't change
    }


    @Test
    void updateUser_notFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(1L, updateDTO));
    }

    @Test
    void updateUser_emailConflict() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        // Simulate new email conflicting with another user (user2's email)
        when(userRepository.existsByEmail(updateDTO.getEmail().toLowerCase(Locale.ROOT))).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.updateUser(1L, updateDTO));
    }

    @Test
    void deleteUserById_success() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);
        userService.deleteUserById(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteUserById_notFound() {
        when(userRepository.existsById(1L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUserById(1L));
    }
}
