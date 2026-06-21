package com.api.artezans.config.utils;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;


@Component
public class SoftHasher {

    private final String salt;

    public SoftHasher(@Value("${artezan.security.email.salt}") String salt) {
        this.salt = salt;
    }

    public String encode(String email) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String input = email.trim().toLowerCase() + salt;
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    public boolean verify(String rawEmail, String hashedEmail) {
        return encode(rawEmail).equals(hashedEmail);
    }
}