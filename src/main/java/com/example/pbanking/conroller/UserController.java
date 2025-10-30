package com.example.pbanking.conroller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.pbanking.dto.AddConsentRequest;
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

    // Test method
    @PostMapping("/consents")
    public ResponseEntity<String> addConsent(@RequestBody AddConsentRequest request) {
        userService.addConsent(request);
        return ResponseEntity.ok().body("Add consent");
    }


}
