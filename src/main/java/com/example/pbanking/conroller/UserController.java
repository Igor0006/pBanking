package com.example.pbanking.conroller;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.pbanking.dto.request.AuthUserRequest;
import com.example.pbanking.dto.response.AuthResponse;
import com.example.pbanking.dto.response.UserInformation;
import com.example.pbanking.model.enums.UserStatus;
import com.example.pbanking.service.UserService;

import lombok.AllArgsConstructor;


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
    
    @GetMapping("/generalData")
    public ResponseEntity<UserInformation> getMethodName() {
        return ResponseEntity.status(201).body(userService.getUserInfo());
    }
    
    @PostMapping("/activatePremium/{days}")
    public ResponseEntity<Void> postMethodName(@PathVariable int days) {
        userService.setStatus(UserStatus.PREMIUM, days);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(null);
    }
}
