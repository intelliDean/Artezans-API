package com.api.artezans.password.service;


import com.api.artezans.users.models.User;

import java.util.Optional;

public interface PasswordResetTokenService {
    void saveToken(User user, String token);
    String validatePasswordResetToken(String passwordResetToken);
    Optional<User> findUserByPasswordToken(String passwordResetToken);
}
