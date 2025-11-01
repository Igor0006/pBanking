package com.example.pbanking.service;

import org.springframework.stereotype.Service;

import com.example.pbanking.dto.CreateUserRequest;
import com.example.pbanking.model.User;
import com.example.pbanking.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    // TODO: В будущем получение юзера через SecurityContextHolder
    public User getCurrentUser() {
        return userRepository.findAll()
                .stream()
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("No users found in the system"));
    }

    public void createUser(CreateUserRequest request) {
        User user = new User(request.username(), request.password());
        userRepository.save(user);
    }
}
