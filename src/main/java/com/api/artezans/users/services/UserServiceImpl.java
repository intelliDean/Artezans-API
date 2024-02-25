package com.api.artezans.users.services;

import com.api.artezans.config.security.JwtService;
import com.api.artezans.config.security.SecuredUser;
import com.api.artezans.exceptions.TaskHubException;
import com.api.artezans.exceptions.TokenException;
import com.api.artezans.exceptions.UserNotFoundException;
import com.api.artezans.multimedia.MultimediaService;
import com.api.artezans.notifications.mail.MailService;
import com.api.artezans.notifications.mail.dto.EmailRequest;
import com.api.artezans.notifications.mail.dto.MailInfo;
import com.api.artezans.password.service.PasswordResetTokenService;
import com.api.artezans.tokens.model.TaskHubVerificationToken;
import com.api.artezans.tokens.service.interfaces.TaskHubVerificationTokenService;
import com.api.artezans.users.models.User;
import com.api.artezans.users.models.enums.AccountState;
import com.api.artezans.users.repository.UserRepository;
import com.api.artezans.utils.ApiResponse;
import com.api.artezans.utils.TaskHubUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Collections;
import java.util.Optional;

import static com.api.artezans.utils.ApiResponse.apiResponse;
import static com.api.artezans.utils.TaskHubUtils.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final TaskHubVerificationTokenService taskHubVerificationTokenService;
    private final PasswordResetTokenService passwordResetTokenService;
    private final MultimediaService multimediaService;
    private final TemplateEngine templateEngine;
    private final UserRepository userRepository;
    private final MailService mailService;
    private final JwtService jwtService;
    private final ResourceLoader resourceLoader;

    @Value("${frontend_url}")
    private String frontendUrl;

    @Override
    public User findUserByEmail(String email) {
        return userRepository.findUserByEmailAddressIgnoreCase(email)
                .orElseThrow(UserNotFoundException::new);
    }

    @Override
    public void validateExistence(String email) {
        if (userRepository.existsByEmailAddress(email)) {
            throw new TaskHubException("User with email " + email + " already exists");
        }
    }

    @Override
    public void sendVerificationMail(User user) {
        final String firstName = user.getFirstName();
        final String url = saveTokenAndGenerateUrl(user, "%s/auth/activating".formatted(frontendUrl));

        sendMail(user, url);
    }

    @Override
    public void sendMail(User user, String url) {
        final Context context = getContext(user.getFirstName());
        final String content = templateEngine.process("verification_mail", context);
        EmailRequest mailRequest = EmailRequest.builder()
                .to(Collections.singletonList(
                        new MailInfo(user.getFirstName(), user.getEmailAddress())))
                .subject("Email verification")
                .htmlContent(content.formatted(url, url, url))
                .build();
        mailService.sendMail(mailRequest);
    }

    private String saveTokenAndGenerateUrl(User user, String url) {
        final String token = generateToken(12);
        final String email = user.getEmailAddress();
        TaskHubVerificationToken verificationToken;
        verificationToken = TaskHubVerificationToken.builder()
                .token(token)
                .emailAddress(email)
                .generatedAt(LocalDateTime.now())
                .expireAt(LocalDateTime.now().plusHours(24))
                .revoked(false)
                .expired(false)
                .build();
        taskHubVerificationTokenService.saveToken(verificationToken);

        String hashedEmail = new BCryptPasswordEncoder().encode(email);
        return TaskHubUtils.getUrl(hashedEmail, token, url);
    }


    public String readHtmlFile(String fileName) throws IOException {
        Resource resource = resourceLoader.getResource("classpath:" + fileName);
        StringBuilder builder = new StringBuilder();
        try (InputStream inputStream = resource.getInputStream();
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
                builder.append(System.lineSeparator());
            }
        }
        return builder.toString();
    }


    private static Context getContext(String firstName) {
        final Context context = new Context();
        context.setVariable("firstName", firstName);
        return context;
    }

    @Override
    public void saveUser(User user) {
        userRepository.save(user);
    }

    @Override
    public User currentUser() {
        try {
            SecuredUser securedUser = (SecuredUser) SecurityContextHolder
                    .getContext()
                    .getAuthentication()
                    .getPrincipal();
            return securedUser.getUser();
        } catch (Exception e) {
            throw new TaskHubException("User not authenticated");
        }
    }


    @Override
    public ApiResponse verified(String token, String email) {
        TaskHubVerificationToken taskHubVerificationToken =
                taskHubVerificationTokenService.findByToken(token);
        String rawEmail = taskHubVerificationToken.getEmailAddress();

        if (taskHubVerificationTokenService.isValid(taskHubVerificationToken)
                && new BCryptPasswordEncoder().matches(rawEmail, email)) {
            //       taskHubVerificationToken.setRevoked(true);
            User user = findUserByEmail(rawEmail);
            user.setAccountState(AccountState.VERIFIED);
            userRepository.save(user);
            taskHubVerificationTokenService.deleteToken(taskHubVerificationToken);
            //       taskHubVerificationTokenService.saveToken(taskHubVerificationToken);
            return apiResponse("Your email is verified successfully. Please proceed to login");
        } else {
            throw new TaskHubException("Verification failed!");
        }
    }

    @Override
    public ApiResponse uploadProfilePicture(MultipartFile image) {
        String imageUrl;
        try {
            imageUrl = multimediaService.upload(image);
        } catch (Exception ex) {
            throw new TaskHubException(ex.getMessage());
        }
        User user = currentUser();
        user.setProfileImage(imageUrl);
        userRepository.save(user);
        return apiResponse("Image uploaded successfully");
    }

    @Override
    public User getUserFromToken(String token) {
        String email = jwtService.extractUsername(token);
        return findUserByEmail(email);
    }

    @Override
    public void savePasswordResetToken(Optional<User> user, String passwordToken) {
        passwordResetTokenService.saveToken(user.get(), passwordToken);
    }

    @Override
    public String verifyPasswordResetToken(String token) {
        return passwordResetTokenService.validatePasswordResetToken(token);
    }

    @Override
    public User findUserByPasswordToken(String token) {
        return passwordResetTokenService.findUserByPasswordToken(token)
                .orElseThrow(() -> new TokenException("Invalid password reset token"));
    }


    @Override
    public void sendPasswordResetMail(User user, String url) {
        final Context context = getContext(user.getFirstName());
        String content = templateEngine.process("reset_password", context);
        EmailRequest emailRequest = EmailRequest.builder()
                .to(Collections.singletonList(
                        new MailInfo(user.getFirstName(), user.getEmailAddress())))
                .subject("Reset Password")
                .htmlContent(content.formatted(url, url, url))
                .build();

        mailService.sendMail(emailRequest);
    }

    @Override
    public ApiResponse deactivateAccount() {
        User user = currentUser();
        user.setAccountState(AccountState.DEACTIVATED);
        user.setDeactivatedAt(LocalDate.now());
        userRepository.save(user);
        return apiResponse("Account deactivated successfully");
    }

    @Override
    public ApiResponse sendActivationMail(String emailAddress, HttpServletRequest request) {
        User user = findUserByEmail(emailAddress);
        if (user.getAccountState().equals(AccountState.DEACTIVATED)) {
            final String firstName = user.getFirstName();
            String requestUrl = applicationUrl(request) + "api/v1/user/reactivate";
            final String url = saveTokenAndGenerateUrl(user, requestUrl);

            final Context context = getContext(firstName);
            final String content = templateEngine.process("reactivation_mail", context);
            EmailRequest emailRequest = EmailRequest.builder()
                    .to(Collections.singletonList(new MailInfo(firstName, user.getEmailAddress())))
                    .subject("Account Reactivation")
                    .htmlContent(content.formatted(url, url, url))
                    .build();
            try {
                mailService.sendMail(emailRequest);
            } catch (Exception e) {
                throw new TaskHubException("Email sending failed");
            }
            return apiResponse("Email sent. Please check your inbox");
        }
        throw new TaskHubException("Unauthorized!");
    }

    private String requestUrl(HttpServletRequest request) {
        return "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
    }

    public ApiResponse reactivate(String hashedEmail, String token) {
        TaskHubVerificationToken taskHubVerificationToken =
                taskHubVerificationTokenService.findByToken(token);
        String rawEmail = taskHubVerificationToken.getEmailAddress();

        if (taskHubVerificationTokenService.isValid(taskHubVerificationToken)
                && new BCryptPasswordEncoder().matches(rawEmail, hashedEmail)) {
            taskHubVerificationToken.setRevoked(true);
            taskHubVerificationTokenService.saveToken(taskHubVerificationToken);
            User user = findUserByEmail(rawEmail);
            log.info("user to work on <<{}>>", user);
            Period period = Period.between(user.getDeactivatedAt(), LocalDate.now());
            int months = period.getMonths();
            if (months >= 1 && months <= 6) {
                user.setAccountState(AccountState.VERIFIED);
                User user1 = userRepository.save(user);
                return apiResponse(REACTIVATED);
            } else {
                if (months < 1) {
                    return apiResponse(BEFORE_ONE_MONTH);
                } else {
                    return apiResponse(AFTER_SIX_MONTH);
                }
            }
        } else {
            throw new TaskHubException("Token has expired");
        }
    }
}