package com.api.artezans.config.Oauth2.userDetail;

import java.util.Map;

import static org.springframework.security.config.oauth2.client.CommonOAuth2Provider.GITHUB;
import static org.springframework.security.config.oauth2.client.CommonOAuth2Provider.GOOGLE;


public class OAuth2UserDataFactory {
    private OAuth2UserDataFactory() {}

    public static Oauth2UserData getOauth2UserData(String registrationId, Map<String, Object> attributes){
        if (registrationId.equalsIgnoreCase(GOOGLE.name()))
            return new GoogleOauth2UserData(attributes);
        if (registrationId.equalsIgnoreCase(GITHUB.name()))
            return new GithubOauth2UserData(attributes);
        return null;
    }
}