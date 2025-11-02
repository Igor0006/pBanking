package com.example.pbanking.service;

import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class EncryptionService {
    private final TextEncryptor textEncryptor;
    private final String authSecretKey;
    private final BCryptPasswordEncoder encoder;

    public EncryptionService(@Value("${encryption.secret-key}") String secretKey, BCryptPasswordEncoder encoder)
            throws NoSuchAlgorithmException {
        String salt = KeyGenerators.string().generateKey();
        this.textEncryptor = Encryptors.delux(secretKey, salt);
        SecretKey authSecretKey = KeyGenerator.getInstance("HmacSHA256").generateKey();
        this.authSecretKey = Base64.getEncoder().encodeToString(authSecretKey.getEncoded());
        this.encoder = encoder;
    }

    public String encrypt(String data) {
        if (data == null)
            return null;
        return textEncryptor.encrypt(data);
    }

    public String decrypt(String encryptedData) {
        if (encryptedData == null)
            return null;
        return textEncryptor.decrypt(encryptedData);
    }

    public SecretKey getAuthSecretKey() {
        byte[] keyBytes = Decoders.BASE64.decode(authSecretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String encodePassword(String password) {
        return encoder.encode(password);
    }

    public boolean isValidPassword(String password, String encodedPassword) {
        return encoder.matches(password, encodedPassword);
    }

}
