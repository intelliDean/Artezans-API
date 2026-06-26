package com.api.artezans.config.security;

import com.api.artezans.exceptions.UserNotFoundException;
import com.api.artezans.users.models.User;
import com.api.artezans.users.services.UserService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {
    private final UserService userService;

    public @NonNull UserDetails loadUserByUsername(@NonNull String emailAddress) throws UsernameNotFoundException {
        try {

            User user = userService.findUserByEmail(emailAddress);
            return new SecuredUser(user);

        } catch (UserNotFoundException e) {
            throw new UsernameNotFoundException("Invalid email or password", e);
        }
    }
}
