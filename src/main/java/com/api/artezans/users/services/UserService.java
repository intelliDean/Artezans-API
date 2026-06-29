package com.api.artezans.users.services;

import com.api.artezans.password.change_password.dtos.EmailParam;
import com.api.artezans.password.change_password.dtos.ResetPasswordRequest;
import com.api.artezans.users.dto.UserDTO;
import com.api.artezans.users.dto.UserMailInfo;
import com.api.artezans.users.models.User;
import com.api.artezans.utils.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {

    User findUserByEmail(String email);

    UserDTO findUserDTOByEmail(String emailAddress);

    void validateUserExistenceByEmail(String email);

    void sendVerificationMail(UserMailInfo user);

    void sendMail(UserMailInfo user, String url);

    ApiResponse verifyUserEmail(String token, String email);

    void saveUser(User user);

    ApiResponse uploadProfilePicture(MultipartFile image, User user);

    User getUserFromToken(String token);

    void savePasswordResetToken(User user, String passwordToken);

    void verifyPasswordResetToken(String token);

    User findUserByPasswordToken(String token);

    void sendPasswordResetMail(User user, String token);

    ApiResponse createLinkForPasswordRequest(EmailParam emailParam);

    ApiResponse resetPassword(ResetPasswordRequest passwordResetRequest, String token);

    ApiResponse deactivateAccount(User user);

    ApiResponse sendActivationMail(String emailAddress, HttpServletRequest request);

    ApiResponse reactivate(String email, String token);

    List<User> findAllUsers();

    ApiResponse enableUser(Long userId);

    ApiResponse disableUser(Long userId);
}

