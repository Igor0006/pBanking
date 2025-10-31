package com.example.pbanking.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.stereotype.Service;

@Service
public class EncryptionService {
    private final TextEncryptor textEncryptor;

    public EncryptionService(@Value("${encryption.secret-key}") String secretKey) {
        String salt = KeyGenerators.string().generateKey();
        this.textEncryptor = Encryptors.delux(secretKey, salt);
    }

    public String encrypt(String data) {
        if (data == null) return null;
        return textEncryptor.encrypt(data);
    }

    public String decrypt(String encryptedData) {
        if (encryptedData == null) return null;
        return textEncryptor.decrypt(encryptedData);
    }
}
