package com.api.artezans.config.utils;


import com.api.artezans.config.security.SecuredUser;
import com.api.artezans.exceptions.ArtezanException;
import com.api.artezans.users.models.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;


@Component
public class SecurityUtils {

    private final String salt;

    public SecurityUtils(@Value("${artezan.security.email.salt}") String salt) {
        this.salt = salt;
    }

    public String hash(String email) {
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
        return hash(rawEmail).equals(hashedEmail);
    }

    public User getCurrentUser() { //If I need current user and I can't pass it through the controller
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ArtezanException("User not authenticated");
        }
        return ((SecuredUser) Objects.requireNonNull(authentication.getPrincipal())).getUser();
    }

}