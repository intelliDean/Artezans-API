package com.api.artezans.users.services;

import com.api.artezans.users.models.User;
import com.api.artezans.utils.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface UserService {

    User findUserByEmail(String email);
    void validateExistence(String email);

    void sendVerificationMail(User user);

    void sendMail(User user, String url);

    ApiResponse verified(String token, String email);

    void saveUser(User user);

    User currentUser();

    ApiResponse uploadProfilePicture(MultipartFile image);

    User getUserFromToken(String token);

    void savePasswordResetToken(Optional<User> user, String passwordToken);

    String verifyPasswordResetToken(String token);

    User findUserByPasswordToken(String token);

    void sendPasswordResetMail(User user, String url);
    ApiResponse deactivateAccount();

    ApiResponse sendActivationMail(String emailAddress, HttpServletRequest request);
    ApiResponse reactivate(String email, String token);
}

