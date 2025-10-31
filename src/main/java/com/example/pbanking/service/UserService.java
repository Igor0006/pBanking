package com.example.pbanking.service;

import org.springframework.stereotype.Service;

import com.example.pbanking.dto.AddConsentRequest;
import com.example.pbanking.dto.CreateUserRequest;
import com.example.pbanking.model.Consent;
import com.example.pbanking.model.User;
import com.example.pbanking.repository.ConsentRepository;
import com.example.pbanking.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final ConsentRepository consentRepository;

    // TODO: В будущем получение юзера через SecurityContextHolder
    public User getCurrentUser() {
        return userRepository.findAll().get(0);
    }

    public void createUser(CreateUserRequest request) {
        User user = new User(request.username(), request.password());
        userRepository.save(user);
    }


    public void addConsent(AddConsentRequest request) {
        User user = userRepository
        .findById(request.userId())
        .orElseThrow(() -> new EntityNotFoundException("No such user"));
        Consent consent = new Consent();
        consent.setBank(request.bank());
        consent.setConsent(request.consent());
        consent.setUser(user);
        consentRepository.save(consent);
    }
}
