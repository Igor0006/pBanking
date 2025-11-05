package com.example.pbanking.service;

import java.time.Instant;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.pbanking.dto.UserPrincipal;
import com.example.pbanking.model.User;
import com.example.pbanking.model.enums.UserStatus;
import com.example.pbanking.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (user.getStatus() == UserStatus.PREMIUM &&
            user.getStatusExpireDate() != null &&
            user.getStatusExpireDate().isBefore(Instant.now())) {
                user.setStatus(UserStatus.DEFAULT);
                userRepository.save(user);
        }
        return new UserPrincipal(user); 
    }
}

