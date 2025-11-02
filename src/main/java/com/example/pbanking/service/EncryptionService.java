package com.example.pbanking.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.stereotype.Service;

/**
 * Provides symmetric encryption utilities for storing sensitive data
 * and delegates password hashing to BCrypt.
 */
@Service
public class EncryptionService {
    private final String secretKey;
    private final BCryptPasswordEncoder passwordEncoder;

    public EncryptionService(@Value("${encryption.secret-key}") String secretKey,
                             BCryptPasswordEncoder passwordEncoder) {
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException("Property 'encryption.secret-key' must be provided.");
        }
        this.secretKey = secretKey;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Encrypts plain text data using AES with a random salt.
     * The salt is prepended to the cipher text using the format {@code salt:cipherHex}.
     */
    public String encrypt(String data) {
        if (data == null) {
            return null;
        }
        String salt = KeyGenerators.string().generateKey();
        TextEncryptor encryptor = Encryptors.delux(secretKey, salt);
        return salt + ":" + encryptor.encrypt(data);
    }

    /**
     * Decrypts text produced by {@link #encrypt(String)}.
     */
    public String decrypt(String stored) {
        if (stored == null) {
            return null;
        }
        int idx = stored.indexOf(':');
        if (idx <= 0) {
            throw new IllegalArgumentException("Encrypted value has no salt prefix (expected 'salt:cipherHex').");
        }
        String salt = stored.substring(0, idx);
        String cipherHex = stored.substring(idx + 1);
        TextEncryptor encryptor = Encryptors.delux(secretKey, salt);
        return encryptor.decrypt(cipherHex);
    }

    public String encodePassword(String password) {
        if (password == null) {
            return null;
        }
        return passwordEncoder.encode(password);
    }

    public boolean isValidPassword(String password, String encodedPassword) {
        if (password == null || encodedPassword == null) {
            return false;
        }
        return passwordEncoder.matches(password, encodedPassword);
    }
}
