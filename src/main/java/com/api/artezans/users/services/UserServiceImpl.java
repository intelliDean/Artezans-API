package com.api.artezans.users.services;

import com.api.artezans.config.security.JwtService;
import com.api.artezans.config.utils.SecurityUtils;
import com.api.artezans.exceptions.ArtezanException;
import com.api.artezans.exceptions.TokenException;
import com.api.artezans.exceptions.UserNotFoundException;
import com.api.artezans.multimedia.MultimediaService;
import com.api.artezans.notifications.mail.MailService;
import com.api.artezans.notifications.dto.EmailRequest;
import com.api.artezans.notifications.dto.MailInfo;
import com.api.artezans.password.change_password.dtos.EmailParam;
import com.api.artezans.password.change_password.dtos.ResetPasswordRequest;
import com.api.artezans.password.forgot_password.model.ForgotPasswordToken;
import com.api.artezans.password.forgot_password.services.ForgotPasswordTokenService;
import com.api.artezans.tokens.model.ArtezanVerificationToken;
import com.api.artezans.tokens.service.interfaces.ArtezanVerificationTokenService;
import com.api.artezans.users.dto.UserDTO;
import com.api.artezans.users.dto.UserMailInfo;
import com.api.artezans.users.dto.UserMapper;
import com.api.artezans.users.models.User;
import com.api.artezans.users.models.enums.AccountState;
import com.api.artezans.users.repository.UserRepository;
import com.api.artezans.utils.ApiResponse;
import com.api.artezans.utils.ArtezanUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

import static com.api.artezans.utils.ApiResponse.apiResponse;
import static com.api.artezans.utils.ArtezanUtils.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
    private final ArtezanVerificationTokenService artezanVerificationTokenService;
    private final ForgotPasswordTokenService forgotPasswordTokenService;
    private final MultimediaService multimediaService;
    private final TemplateEngine templateEngine;
    private final UserRepository userRepository;
    private final SecurityUtils softHash;
    private final MailService mailService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final ResourceLoader resourceLoader;
    private final UserMapper userMapper;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Override
    public User findUserByEmail(String email) {
        return userRepository.findUserByEmailAddressIgnoreCase(email)
                .orElseThrow(UserNotFoundException::new);
    }

    @Override
    public UserDTO findUserDTOByEmail(String emailAddress) {
        User user = userRepository.findUserByEmailAddressIgnoreCase(emailAddress)
                .orElseThrow(UserNotFoundException::new);

        return userMapper.toDTO(user);
    }

    @Override
    public void validateUserExistenceByEmail(String email) {
        if (userRepository.existsByEmailAddress(email)) {
            throw new ArtezanException("User with email %s already exists".formatted(email));
        }
    }

    @Override
    public void sendVerificationMail(UserMailInfo userInfo) {
        sendMail(
                userInfo,
                saveTokenAndGenerateUrl(userInfo.emailAddress(), "%s/auth/activating".formatted(frontendUrl)));
    }

    @Override
    public void sendMail(UserMailInfo userInfo, String url) {
        final Context context = getContext(userInfo.firstName());
        final String content = templateEngine.process("verification_mail", context);

        EmailRequest mailRequest = EmailRequest.builder()
                .to(Collections.singletonList(new MailInfo(userInfo.firstName(), userInfo.emailAddress())))
                .subject("Email verification")
                .htmlContent(content.formatted(url, url, url))
                .build();
        mailService.sendMail(mailRequest);
    }

    private String saveTokenAndGenerateUrl(String email, String url) {
        final String token = generateToken(12);

        ArtezanVerificationToken verificationToken = ArtezanVerificationToken.builder()
                .token(token)
                .emailAddress(email)
                // .generatedAt(LocalDateTime.now())
                .expireAt(LocalDateTime.now().plusHours(24)) //
                .revoked(false)
                .expired(false)
                .build();
        artezanVerificationTokenService.saveToken(verificationToken);

        // String hashedEmail = new BCryptPasswordEncoder().encode(email);
        return ArtezanUtils.buildActionUrl(url, token, softHash.hash(email));
    }

    @Override
    public ApiResponse createLinkForPasswordRequest(EmailParam emailParam) {
        User user = findUserByEmail(emailParam.email());

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cooldownLimit = now.minusMinutes(2);
        if (forgotPasswordTokenService.existsValidTokenGeneratedSince(user.getEmailAddress(), now, cooldownLimit)) {
            throw new ArtezanException("Password reset requested too recently. Please wait 2 minutes before requesting another.");
        }

        String passwordToken = ArtezanUtils.generateToken(12);
        savePasswordResetToken(user, passwordToken);
        sendPasswordResetMail(user, passwordToken);
        return apiResponse("Password reset email has been sent to your mail");
    }

    public ApiResponse resetPassword(ResetPasswordRequest passwordResetRequest, String token) {
        User user = findUserByPasswordToken(token);

        if (passwordEncoder.matches(passwordResetRequest.password(), user.getPassword())) {
            return apiResponse("Password reset successful");
        }

        verifyPasswordResetToken(token);

        resetUserPassword(user, passwordResetRequest.password());
        return apiResponse("Password reset successful");
    }

    private void resetUserPassword(User user, String password) {
        user.setPassword(passwordEncoder.encode(password));
        saveUser(user);
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
    public ApiResponse verifyUserEmail(String token, String hashedEmail) {
        ArtezanVerificationToken artezanVerificationToken = artezanVerificationTokenService.findByToken(token);
        String rawEmail = artezanVerificationToken.getEmailAddress();

        if (artezanVerificationTokenService.isValid(artezanVerificationToken)
                && softHash.verify(rawEmail, hashedEmail)) {
            User user = findUserByEmail(rawEmail);
            user.setAccountState(AccountState.VERIFIED);
            userRepository.save(user);
            artezanVerificationTokenService.deleteToken(artezanVerificationToken);
            // taskHubVerificationTokenService.saveToken(taskHubVerificationToken);
            return apiResponse("Your email is verified successfully. Please proceed to login");
        } else {
            throw new ArtezanException("Verification failed!");
        }
    }

    @Override
    public ApiResponse uploadProfilePicture(MultipartFile image, User user) {
        String imageUrl;
        try {
            imageUrl = multimediaService.upload(image);
        } catch (Exception ex) {
            throw new ArtezanException(ex.getMessage());
        }
        // User user = currentUser();
        user.setProfileImage(imageUrl);
        userRepository.save(user);
        return apiResponse("Image uploaded successfully");
    }

    @Override
    public User getUserFromToken(String token) {
        return findUserByEmail(
                jwtService.extractUsernameFromToken(token));
    }

    @Override
    public void savePasswordResetToken(User optionalUser, String passwordToken) {
        forgotPasswordTokenService.saveToken(optionalUser, passwordToken);
    }

    @Override
    public void verifyPasswordResetToken(String token) {
        forgotPasswordTokenService.validatePasswordResetToken(token);
    }

    @Override
    public User findUserByPasswordToken(String token) {
        return forgotPasswordTokenService.findUserByPasswordToken(token)
                .orElseThrow(() -> new TokenException("Invalid password reset token"));
    }

    @Override
    public void sendPasswordResetMail(User user, String token) {

        String url = "%s/auth/reset-password?t=%s".formatted(frontendUrl, token);

        final Context context = getContext(user.getFirstName());
        String content = templateEngine.process("reset_password", context);
        EmailRequest emailRequest = EmailRequest.builder()
                .to(Collections.singletonList(
                        new MailInfo(user.getFirstName(), user.getEmailAddress())))
                .subject("Reset Password")
                .htmlContent(content.formatted(url, url, url))
                .build();
        try {
            mailService.sendMail(emailRequest);
        } catch (Exception e) {
            throw new ArtezanException("Error sending change of password email");
        }
    }

    @Override
    public ApiResponse deactivateAccount(User user) {
        // User user = currentUser();
        user.setAccountState(AccountState.DEACTIVATED);
        user.setDeactivatedAt(LocalDate.now());
        userRepository.save(user);
        return apiResponse("Account deactivated successfully");
    }

    @Override
    public ApiResponse sendActivationMail(String emailAddress, HttpServletRequest request) {
        User user = findUserByEmail(emailAddress);
        if (user.getAccountState().equals(AccountState.DEACTIVATED)) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime cooldownLimit = now.minusMinutes(2);
            if (artezanVerificationTokenService.existsValidTokenGeneratedSince(user.getEmailAddress(), now, cooldownLimit)) {
                throw new ArtezanException("Reactivation email requested too recently. Please wait 2 minutes before requesting another.");
            }
            final String firstName = user.getFirstName();
            String requestUrl = applicationUrl(request) + "/api/v1/user/reactivate";
            final String url = saveTokenAndGenerateUrl(user.getEmailAddress(), requestUrl);

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
                throw new ArtezanException("Email sending failed");
            }
            return apiResponse("Email sent. Please check your inbox");
        }
        throw new ArtezanException("Unauthorized!");
    }

    public ApiResponse reactivate(String hashedEmail, String token) {
        ArtezanVerificationToken artezanVerificationToken = artezanVerificationTokenService.findByToken(token);
        String rawEmail = artezanVerificationToken.getEmailAddress();

        if (artezanVerificationTokenService.isValid(artezanVerificationToken) && softHash.verify(rawEmail, hashedEmail)) {

            artezanVerificationToken.setRevoked(true);
            artezanVerificationTokenService.saveToken(artezanVerificationToken);

            User user = findUserByEmail(rawEmail);
            log.info("Reactivation attempt for user <<{}>>", user.getEmailAddress());

            // ChronoUnit.MONTHS gives total elapsed months across year boundaries.
            // Period.getMonths() only returns the month component (0–11), so it would
            // incorrectly allow reactivation at month 13 (period.getMonths() == 1).
            long totalMonths = ChronoUnit.MONTHS.between(user.getDeactivatedAt(), LocalDate.now());

            if (totalMonths < 1) {
                return apiResponse(BEFORE_ONE_MONTH);
            }
            if (totalMonths > 6) {
                return apiResponse(AFTER_SIX_MONTH);
            }

            user.setAccountState(AccountState.VERIFIED);
            userRepository.save(user);
            return apiResponse(REACTIVATED);

        } else {
            throw new ArtezanException("Token has expired");
        }
    }

    @Override
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public ApiResponse enableUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ArtezanException("User not found"));
        user.setEnabled(true);
        user.setAccountState(AccountState.VERIFIED);
        userRepository.save(user);
        return apiResponse("User account enabled successfully");
    }

    @Override
    public ApiResponse disableUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ArtezanException("User not found"));
        user.setEnabled(false);
        user.setAccountState(AccountState.DEACTIVATED);
        userRepository.save(user);
        return apiResponse("User account suspended/disabled successfully");
    }
}