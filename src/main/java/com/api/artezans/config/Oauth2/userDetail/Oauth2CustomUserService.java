package com.api.artezans.config.Oauth2.userDetail;

import com.api.artezans.config.security.SecuredUser;
import com.api.artezans.exceptions.UserNotFoundException;
import com.api.artezans.users.models.User;
import com.api.artezans.users.models.enums.AccountState;
import com.api.artezans.users.models.enums.Role;
import com.api.artezans.users.services.UserService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.UUID;

@Service
@AllArgsConstructor
public class Oauth2CustomUserService extends DefaultOAuth2UserService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        try {
            return processOAuth2User(userRequest, oAuth2User);
        } catch (Exception ex) {
            throw new OAuth2AuthenticationException(new OAuth2Error("oauth2_error"), ex.getMessage(), ex);
        }
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        Oauth2UserData oAuth2UserData = OAuth2UserDataFactory.getOauth2UserData(registrationId, oAuth2User.getAttributes());
        if (oAuth2UserData == null) {
            throw new OAuth2AuthenticationException(new OAuth2Error("oauth2_error"), "OAuth2 provider not supported: " + registrationId);
        }

        String email = oAuth2UserData.getEmail();
        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException(new OAuth2Error("oauth2_error"), "Email address not found from OAuth2 provider: " + registrationId);
        }

        User user;
        try {
            user = updateExistingUser(userService.findUserByEmail(email), oAuth2UserData);
        } catch (UserNotFoundException ex) {
            user = registerNewUser(oAuth2UserData);
        }

        return new SecuredUser(user, oAuth2User.getAttributes());
    }

    private User registerNewUser(Oauth2UserData oAuth2UserData) {
        String name = oAuth2UserData.getName();
        String firstName = "";
        String lastName = "";
        if (name != null && !name.isBlank()) {
            String[] parts = name.split(" ", 2);
            firstName = parts[0];
            lastName = parts.length > 1 ? parts[1] : "";
        }
        if (firstName.isBlank()) {
            firstName = "OAuth2User";
        }

        User user = User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .emailAddress(oAuth2UserData.getEmail())
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .profileImage(oAuth2UserData.getImageUrl())
                .enabled(true)
                .accountState(AccountState.VERIFIED)
                .roles(new HashSet<>(Collections.singletonList(Role.CUSTOMER)))
                .build();

        userService.saveUser(user);
        return user;
    }

    private User updateExistingUser(User existingUser, Oauth2UserData oAuth2UserData) {
        String name = oAuth2UserData.getName();
        if (name != null && !name.isBlank()) {
            String[] parts = name.split(" ", 2);
            existingUser.setFirstName(parts[0]);
            existingUser.setLastName(parts.length > 1 ? parts[1] : "");
        }
        if (oAuth2UserData.getImageUrl() != null) {
            existingUser.setProfileImage(oAuth2UserData.getImageUrl());
        }
        // Force account to be verified/enabled if they log in successfully via OAuth2
        existingUser.setAccountState(AccountState.VERIFIED);
        existingUser.setEnabled(true);

        userService.saveUser(existingUser);
        return existingUser;
    }
}