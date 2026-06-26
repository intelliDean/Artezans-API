package com.api.artezans.password.forgot_password.services;


import com.api.artezans.password.forgot_password.model.ForgotPasswordToken;
import com.api.artezans.users.models.User;

import java.util.Optional;

//public interface ForgotPasswordTokenService {
//    void saveToken(User user, String token);
//    String validatePasswordResetToken(String passwordResetToken);
//    Optional<User> findUserByPasswordToken(String passwordResetToken);
//}


public interface ForgotPasswordTokenService {
    void saveToken(User user, String token);
    void validatePasswordResetToken(String token);
    Optional<User> findUserByPasswordToken(String token);
    void revokeToken(ForgotPasswordToken token);
    void deleteAllInvalidTokens();
    boolean existsValidTokenGeneratedSince(String email, java.time.LocalDateTime now, java.time.LocalDateTime cooldownLimit);
}