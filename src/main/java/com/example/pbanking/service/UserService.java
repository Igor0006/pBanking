package com.example.pbanking.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.pbanking.dto.BankClientLink;
import com.example.pbanking.dto.request.AuthUserRequest;
import com.example.pbanking.dto.response.AuthResponse;
import com.example.pbanking.dto.response.UserInformation;
import com.example.pbanking.exception.ConflictException;
import com.example.pbanking.exception.NotFoundException;
import com.example.pbanking.exception.UnauthorizedException;
import com.example.pbanking.model.User;
import com.example.pbanking.model.enums.UserStatus;
import com.example.pbanking.repository.CredentialsRepository;

import jakarta.transaction.Transactional;

import com.example.pbanking.repository.UserRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final CredentialsRepository credentialsRepository;
    private final JWTService jwtService;
    private final EncryptionService encryptionService;

    public User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found: " + username));
    }

    public AuthResponse registerUser(AuthUserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new ConflictException("User with name " + request.username() + " already exists");
        }

        User user = new User(request.username(), encryptionService.encodePassword(request.password()));
        userRepository.save(user);
        String token = jwtService.generateToken(request.username());
        return new AuthResponse(token, jwtService.getExpirationTime());
    }

    public AuthResponse loginUser(AuthUserRequest request) {
        User user = userRepository.findByUsername(request.username())
        .orElseThrow(() -> new UnauthorizedException("Invalid username or password"));

        if (!encryptionService.isValidPassword(request.password(), user.getPassword())) {
            throw new UnauthorizedException("Invalid username or password");
        }

        String token = jwtService.generateToken(request.username());
        return new AuthResponse(token, jwtService.getExpirationTime());
    }
    
    public UserInformation getUserInfo() {
        return new UserInformation(getAllBankClientLinks(), getCurrentUser().getStatus());
    }

    public List<BankClientLink> getAllBankClientLinks() {
        return credentialsRepository.findAllBankClientPairs().stream()
                .map(pair -> new BankClientLink(pair.getBankId(), pair.getClientId()))
                .toList();
    }
    
    @Transactional
    public void setStatus(UserStatus status, int days) {
        User u = getCurrentUser();
        u.setStatus(status);
        if (status == UserStatus.PREMIUM)
            u.setStatusExpireDate(Instant.now().plus(days, ChronoUnit.DAYS));
        else {
            u.setStatusExpireDate(null);
        }
    }
}
