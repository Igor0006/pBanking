package com.example.pbanking.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.stereotype.Service;

@Service
public class EncryptionService {

    private final String secretKey;

    public EncryptionService(@Value("${encryption.secret-key}") String secretKey) {
        this.secretKey = secretKey;
    }

    public String encrypt(String data) {
        if (data == null)
            return null;
        String salt = KeyGenerators.string().generateKey();
        TextEncryptor enc = Encryptors.delux(secretKey, salt); // AES-GCM + PBKDF2
        String cipherHex = enc.encrypt(data);
        return salt + ":" + cipherHex;
    }

    public String decrypt(String stored) {
        if (stored == null)
            return null;
        int idx = stored.indexOf(':');
        if (idx <= 0) {
            throw new IllegalArgumentException("Encrypted value has no salt prefix (expected 'salt:cipherHex').");
        }
        String salt = stored.substring(0, idx);
        String cipherHex = stored.substring(idx + 1);
        TextEncryptor enc = Encryptors.delux(secretKey, salt);
        return enc.decrypt(cipherHex);
    }
}