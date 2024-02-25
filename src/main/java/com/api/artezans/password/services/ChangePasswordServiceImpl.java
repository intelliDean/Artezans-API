package com.api.artezans.password.services;

import com.api.artezans.exceptions.TaskHubException;
import com.api.artezans.notifications.mail.MailService;
import com.api.artezans.notifications.mail.dto.EmailRequest;
import com.api.artezans.notifications.mail.dto.MailInfo;
import com.api.artezans.password.data.dtos.ChangePasswordRequest;
import com.api.artezans.password.data.model.ChangePasswordToken;
import com.api.artezans.password.data.repository.ChangePasswordTokenRepository;
import com.api.artezans.users.models.User;
import com.api.artezans.users.services.UserService;
import com.api.artezans.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Collections;
import java.util.List;

import static com.api.artezans.utils.ApiResponse.apiResponse;
import static com.api.artezans.utils.TaskHubUtils.generateToken;
import static org.springframework.transaction.annotation.Propagation.REQUIRED;

@Service
@RequiredArgsConstructor
public class ChangePasswordServiceImpl implements ChangePasswordService {

    @Value("${frontend_url}")
    private String frontendUrl;

    private final ChangePasswordTokenRepository changePasswordTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final TemplateEngine templateEngine;
    private final UserService userService;
    private final MailService mailService;


    @Override
    @Transactional(propagation = REQUIRED)
    public ApiResponse initChangeOfPassword(ChangePasswordRequest passwordRequest, User currentUser) {
        //  User currentUser = userService.currentUser();
        validatePassword(passwordRequest);
        unauthorized(!passwordEncoder.matches(passwordRequest.getOldPassword(),
                currentUser.getPassword()), "Unauthorized!");
        String urlLink = saveTokenAndGenerateLink(passwordRequest, currentUser);
        EmailRequest emailRequest = getEmailRequest(currentUser, urlLink);
        try {
            mailService.sendMail(emailRequest);
        } catch (Exception e) {
            throw new TaskHubException("Error sending change of password mail");
        }
        return apiResponse("Successful! Change of password mail has been sent to your email");
    }

    private static void unauthorized(boolean isTrue, String message) {
        if (isTrue) {
            throw new TaskHubException(message);
        }
    }

    private String saveTokenAndGenerateLink(ChangePasswordRequest passwordRequest, User currentUser) {
        String token = generateToken(24);
        ChangePasswordToken changePasswordToken = ChangePasswordToken.builder()
                .token(token)
                .oldPassword(passwordRequest.getOldPassword())
                .newPassword(passwordRequest.getNewPassword())
                .emailAddress(currentUser.getEmailAddress())
                .build();
        changePasswordTokenRepository.save(changePasswordToken);
        return "%s/auth/change-password?t=%s".formatted(frontendUrl, token);
    }

    private static void validatePassword(ChangePasswordRequest passwordRequest) {
        unauthorized(passwordRequest.getOldPassword().equals(passwordRequest.getNewPassword()),
                "New password is the same as old password");
    }

    private static Context getContext(String firstName) {
        final Context context = new Context();
        context.setVariable("firstName", firstName);
        return context;
    }

    private EmailRequest getEmailRequest(User user, String url) {
        String firstName = user.getFirstName();
        Context context = getContext(firstName);
        final String content = templateEngine.process("change_password_mail", context);
        return EmailRequest.builder()
                .to(Collections.singletonList(MailInfo.builder()
                        .email(user.getEmailAddress())
                        .name(firstName)
                        .build()))
                .subject("Change of Password")
                .htmlContent(content.formatted(url, url, url))
                .build();
    }

    @Override
    @Transactional(propagation = REQUIRED)
    public ApiResponse changePassword(String token) {
        ChangePasswordToken changePasswordToken = getAndValidateToken(token);
        User user = userService.findUserByEmail(changePasswordToken.getEmailAddress());
        unauthorized(!passwordEncoder.matches(
                        changePasswordToken.getOldPassword(), user.getPassword()
                ), "Unauthorized! Incorrect password"
        );
        user.setPassword(passwordEncoder.encode(changePasswordToken.getNewPassword()));
        changePasswordToken.setRevoked(true);
        changePasswordTokenRepository.save(changePasswordToken);
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            SecurityContextHolder.clearContext();
        }
        return apiResponse("Password changed successfully");
    }

    @NotNull
    private ChangePasswordToken getAndValidateToken(String token) {
        ChangePasswordToken changePasswordToken = changePasswordTokenRepository.findByToken(token)
                .orElseThrow(() -> new TaskHubException("Could not find change password token"));
        changePasswordToken.checkTokenExpiration();
        if (changePasswordToken.isExpired()) {
            throw new TaskHubException("Change password token expired");
        }
        return changePasswordToken;
    }

    @Scheduled(cron = "0 0 0 * * ?", zone = "Australia/Sydney") //scheduled to run every midnight
    private void deleteAllRevokedTokens() {
        final List<ChangePasswordToken> allRevokedTokens = changePasswordTokenRepository.findAllRevokedTokens();
        if (!allRevokedTokens.isEmpty()) {
            changePasswordTokenRepository.deleteAll(allRevokedTokens);
        }
    }
}
