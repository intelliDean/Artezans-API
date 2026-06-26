package com.api.artezans.password.change_password.services;

import com.api.artezans.config.security.SecuredUser;
import com.api.artezans.exceptions.ArtezanException;
import com.api.artezans.notifications.mail.MailService;
import com.api.artezans.notifications.dto.EmailRequest;
import com.api.artezans.notifications.dto.MailInfo;
import com.api.artezans.password.change_password.dtos.ChangePasswordRequest;
import com.api.artezans.password.change_password.model.ChangePasswordToken;
import com.api.artezans.password.change_password.repository.ChangePasswordTokenRepository;
import com.api.artezans.users.models.User;
import com.api.artezans.users.services.UserService;
import com.api.artezans.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.Collections;

import static com.api.artezans.utils.ApiResponse.apiResponse;
import static com.api.artezans.utils.ArtezanUtils.generateToken;

@Service
@RequiredArgsConstructor
public class ChangePasswordServiceImpl implements ChangePasswordService {

    @Value("${frontend.url}")
    private String frontendUrl;

    private final ChangePasswordTokenRepository changePasswordTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final TemplateEngine templateEngine;
    private final UserService userService;
    private final MailService mailService;

    @Override
    @Transactional
    public ApiResponse initChangeOfPassword(ChangePasswordRequest request, SecuredUser currentUser) {
        validatePassword(request);
        validate(
                !passwordEncoder.matches(request.oldPassword(), currentUser.getPassword()),
                "Unauthorized!"
        );
        String token = generateToken(24);
        saveToken(request, currentUser, token);
        sendChangePasswordEmail(
                currentUser,
                generateLink(token)
        );
        return apiResponse("Successful! Change of password mail has been sent to your email");
    }

    @Override
    @Transactional
    public ApiResponse completePasswordChange(String token) {
        ChangePasswordToken changePasswordToken = fetchAndValidateToken(token);
        User user = userService.findUserByEmail(changePasswordToken.getEmailAddress());

        user.setPassword(changePasswordToken.getNewPasswordHash());
        userService.saveUser(user);

        changePasswordToken.setRevoked(true);
        changePasswordTokenRepository.save(changePasswordToken);

        SecurityContextHolder.clearContext();
        return apiResponse("Password changed successfully");
    }

    private void saveToken(ChangePasswordRequest request, SecuredUser currentUser, String token) {
        changePasswordTokenRepository.save(
                ChangePasswordToken.builder()
                        .token(token)
                        .newPasswordHash(passwordEncoder.encode(request.newPassword()))
                        .emailAddress(currentUser.getUser().getEmailAddress())
                        .expireAt(LocalDateTime.now().plusHours(24))
                        .build()
        );
    }

    private String generateLink(String token) {
        return "%s/auth/change-password?t=%s".formatted(frontendUrl, token);
    }

    private void sendChangePasswordEmail(SecuredUser user, String url) {
        try {
            mailService.sendMail(getEmailRequest(user.getUser(), url));
        } catch (Exception e) {
            throw new ArtezanException("Error sending change of password mail");
        }
    }

    private ChangePasswordToken fetchAndValidateToken(String token) {
        ChangePasswordToken changePasswordToken = changePasswordTokenRepository
                .findByToken(token)
                .orElseThrow(() -> new ArtezanException("Could not find change password token"));

        if (changePasswordToken.isExpired()) {
            throw new ArtezanException("Token is expired... Initiate password change again");
        }
        return changePasswordToken;
    }

    private static void validatePassword(ChangePasswordRequest request) {
        validate(
                request.oldPassword().equals(request.newPassword()),
                "New password cannot be the same as old password"
        );
    }

    private static void validate(boolean condition, String message) {
        if (condition) throw new ArtezanException(message);
    }

    private EmailRequest getEmailRequest(User user, String url) {
        String firstName = user.getFirstName();
        Context context = new Context();
        context.setVariable("firstName", firstName);
        String content = templateEngine.process("change_password_mail", context);
        return EmailRequest.builder()
                .to(Collections.singletonList(MailInfo.builder()
                        .email(user.getEmailAddress())
                        .name(firstName)
                        .build()))
                .subject("Change of Password")
                .htmlContent(content.formatted(url, url, url))
                .build();
    }

    @Scheduled(cron = "0 0 0 * * ?", zone = "Australia/Sydney")
    void deleteAllInvalidTokens() {
        changePasswordTokenRepository.deleteAllInvalidTokens(LocalDateTime.now());
    }
}
