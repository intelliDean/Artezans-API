package com.api.artezans.config.Oauth2;

import com.api.artezans.config.security.JwtService;
import com.api.artezans.config.security.SecuredUser;
import com.api.artezans.exceptions.ArtezanException;
import com.api.artezans.tokens.model.ArtezanToken;
import com.api.artezans.tokens.service.interfaces.ArtezanTokenService;
import com.api.artezans.users.models.User;
import com.api.artezans.users.services.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;

import static com.api.artezans.config.Oauth2.userDetail.OAuth2Constants.REDIRECT_URI_PARAM_COOKIE_NAME;


@RequiredArgsConstructor
@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtService jwtService;
    private final ArtezanTokenService artezanTokenService;
    private final UserService userService;

//    @Value("${app.oauth2.authorized-redirect-uris}")
//    private List<String> authorizedRedirectUris;

    private final OAuth2Properties oAuth2Properties;

    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

    @Override
    public void onAuthenticationSuccess(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Authentication authentication) throws IOException {
        String targetUrl = determineTargetUrl(request, response, authentication);

        if (response.isCommitted()) {
            logger.debug("Response has already been committed. Unable to redirect to " + targetUrl);
            return;
        }

        clearAuthenticationAttributes(request, response);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    @SneakyThrows
    @Override
    protected @NonNull String determineTargetUrl(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            Authentication authentication) {
        Optional<String> redirectUri = CookieUtils.getCookie(request, REDIRECT_URI_PARAM_COOKIE_NAME)
                .map(Cookie::getValue);

        if (redirectUri.isPresent() && !isAuthorizedRedirectUri(redirectUri.get())) {
            throw new ArtezanException(
                    "Sorry! We've got an Unauthorized Redirect URI and can't proceed with the authentication"
            );
        }
        String targetUrl = redirectUri.orElse(getDefaultTargetUrl());
        SecuredUser securedUser = (SecuredUser) authentication.getPrincipal();
        JwtService.Tokens token = jwtService.generateToken(authentication);
        assert securedUser != null;
        User foundUser = userService.findUserByEmail(securedUser.getUsername());
        artezanTokenService.revokeToken(foundUser.getId());
        saveToken(token, securedUser.getUser());
        return UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("token", token.accessToken())
                .queryParam("refreshToken", token.refreshToken())
                .build().toUriString();
    }

    protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequest(request, response);
    }

    private boolean isAuthorizedRedirectUri(String uri) {
        URI clientRedirectUri = URI.create(uri);
        return oAuth2Properties.getAuthorizedRedirectUris()
                .stream()
                .anyMatch(authorizedRedirectUri -> {
                    URI authorizedURI = URI.create(authorizedRedirectUri);
                    return authorizedURI.getHost().equalsIgnoreCase(clientRedirectUri.getHost())
                            && authorizedURI.getPort() == clientRedirectUri.getPort();
                });
    }

    private void saveToken(JwtService.Tokens token, User user) {
        ArtezanToken artezanToken = ArtezanToken.builder()
                .accessToken(token.accessToken())
                .refreshToken(token.refreshToken())
                .revoked(false)
                .user(user)
                .build();
        artezanTokenService.saveToken(artezanToken);
    }
}
