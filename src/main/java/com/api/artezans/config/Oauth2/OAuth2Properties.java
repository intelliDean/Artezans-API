package com.api.artezans.config.Oauth2;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "app.oauth2")
@Component
@Getter
@Setter
public class OAuth2Properties {
    private List<String> authorizedRedirectUris = new ArrayList<>();
}