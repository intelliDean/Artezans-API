package com.api.artezans.config.security;

import com.api.artezans.users.models.User;
import com.api.artezans.users.models.enums.Role;
import lombok.Getter;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class SecuredUser implements UserDetails, OAuth2User {

    private final User user;

//    private final String firstName;
//
//    private final String lastName;
//
//    private final String emailAddress;
//
//    private final String password;
//
//    private final boolean isEnabled;
//
//    private final Set<Role> roles;

    private Map<String, Object> attributes;

    public SecuredUser(User user) {

        this.user = user;

//        this.firstName = user.getFirstName();
//        this.lastName = user.getLastName();
//        this.emailAddress = user.getEmailAddress();
//        this.isEnabled = user.isEnabled();
//        this.password = user.getPassword();
//        this.roles = user.getRoles();
    }

    public SecuredUser(User user, Map<String, Object> attributes) {
        this(user);
        this.attributes = attributes;
    }

    @Override
    public @NonNull Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .collect(Collectors.toSet());
    }

    @Override
    public @NonNull String getUsername() {
        return user.getEmailAddress();
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public boolean isEnabled() {
        return user.isEnabled();
    }

    @Override
    public @NonNull String getName() {
        return "%s %s".formatted(user.getFirstName(), user.getLastName());
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes != null ? attributes : Map.of();
    }
}