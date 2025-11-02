package com.example.pbanking.conroller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.pbanking.dto.AuthResponse;
import com.example.pbanking.dto.AuthUserRequest;
import com.example.pbanking.dto.CreateUserRequest;
import com.example.pbanking.service.UserService;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/users")
    public ResponseEntity<String> createUser(@RequestBody CreateUserRequest request) {
        userService.createUser(request);
        return ResponseEntity.status(201).body("User created");
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody AuthUserRequest request) {
        AuthResponse response = userService.registerUser(request);
        return ResponseEntity.status(201).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthUserRequest request) {
        AuthResponse response = userService.loginUser(request);
        return ResponseEntity.ok().body(response);
    }
}
