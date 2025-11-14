package com.example.pbanking.user;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.pbanking.common.enums.UserStatus;
import com.example.pbanking.common.security.EncryptionService;
import com.example.pbanking.consent.CredentialsRepository;
import com.example.pbanking.exception.ConflictException;
import com.example.pbanking.exception.NotFoundException;
import com.example.pbanking.exception.UnauthorizedException;
import com.example.pbanking.user.dto.BankClientLink;
import com.example.pbanking.user.dto.request.AuthUserRequest;
import com.example.pbanking.user.dto.response.AuthResponse;
import com.example.pbanking.user.dto.response.UserInformation;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final CredentialsRepository credentialsRepository;
    private final JWTService jwtService;
    private final EncryptionService encryptionService;
    @Value("${secret.team_secret}")
    private String teamSecret;

    @PostConstruct
    public void initAdmin() {
        userRepository.findByUsername("admin")
                .ifPresentOrElse(user -> {
                    boolean requiresUpdate = user.getStatus() != UserStatus.ADMIN;
                    String encodedSecret = encryptionService.encodePassword(teamSecret);
                    if (!encryptionService.isValidPassword(teamSecret, user.getPassword())) {
                        user.setPassword(encodedSecret);
                        requiresUpdate = true;
                    }
                    if (requiresUpdate) {
                        user.setStatus(UserStatus.ADMIN);
                        userRepository.save(user);
                    }
                }, () -> {
                    User admin = new User("admin", encryptionService.encodePassword(teamSecret), UserStatus.ADMIN);
                    userRepository.save(admin);
                });
    }

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
        var u = getCurrentUser();
        var bankClientLinks = getBankClientLinks(u);
        return new UserInformation(bankClientLinks, u.getStatus(), u.getStatusExpireDate(), u.getName(), u.getSurname(),
                u.getUsername());
    }

    public List<BankClientLink> getBankClientLinks(User user) {
        return toBankClientLinks(credentialsRepository.findBankClientPairsByUser(user));
    }

    private List<BankClientLink> toBankClientLinks(List<CredentialsRepository.BankClientPair> pairs) {
        return pairs.stream()
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

    @Transactional
    public void setNameAndSurname(String name, String surname) {
        User u = getCurrentUser();
        u.setName(name);
        u.setSurname(surname);
    }
}
