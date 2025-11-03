package com.example.pbanking.conroller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.pbanking.dto.AuthResponse;
import com.example.pbanking.dto.AuthUserRequest;
import com.example.pbanking.dto.BankClientLink;
import com.example.pbanking.repository.CredentialsRepository.BankClientPair;
import com.example.pbanking.service.UserService;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@AllArgsConstructor
@RequestMapping("api/user")
public class UserController {
    private final UserService userService;

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
    
    @GetMapping("/clientIds")
    public ResponseEntity<List<BankClientPair>> getUsersClientIds() {
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(userService.getUserClientIds());
    }
    
    @GetMapping("/bankClientLinks")
    public ResponseEntity<List<BankClientLink>> getMethodName() {
        return ResponseEntity.status(201).body(userService.getAllBankClientLinks());
    }
    
    
}
