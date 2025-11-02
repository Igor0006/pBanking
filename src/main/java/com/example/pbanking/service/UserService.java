package com.example.pbanking.service;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.pbanking.dto.AuthResponse;
import com.example.pbanking.dto.AuthUserRequest;
import com.example.pbanking.dto.CreateUserRequest;
import com.example.pbanking.model.User;
import com.example.pbanking.repository.UserRepository;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final JWTService jwtService;
    private final EncryptionService encryptionService;

    public User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("No users found in the system"));
    }

    public void createUser(CreateUserRequest request) {
        User user = new User(request.username(), encryptionService.encodePassword(request.password()));
        userRepository.save(user);
    }

    public AuthResponse registerUser(AuthUserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new EntityExistsException("User with name " + request.username() + " already exists");
        }

        User user = new User(request.username(), encryptionService.encodePassword(request.password()));
        userRepository.save(user);
        String token = jwtService.generateToken(request.username());
        return new AuthResponse(token, jwtService.getExpirationTime());
    }

    public AuthResponse loginUser(AuthUserRequest request) {

        User user = userRepository.findByUsername(request.username())
        .orElseThrow(() -> new UsernameNotFoundException("No user with name: " + request.username()));

        if (!encryptionService.isValidPassword(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid password");
        }

        String token = jwtService.generateToken(request.username());
        return new AuthResponse(token, jwtService.getExpirationTime());
    }
}
