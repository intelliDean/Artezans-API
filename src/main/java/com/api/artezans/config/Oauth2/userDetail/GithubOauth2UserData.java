package com.api.artezans.config.Oauth2.userDetail;

import java.util.Map;

public class GithubOauth2UserData extends Oauth2UserData {
    public GithubOauth2UserData(Map<String, Object> attributes) {
        super(attributes);
    }
    @Override
    public String getUserId() {
        return  attributes.get("id").toString();
    }

    @Override
    public String getName() {
        return attributes.get("name").toString();
    }

    @Override
    public String getEmail() {
        return attributes.get("email").toString();
    }

    @Override
    public String getImageUrl() {
        return attributes.get("avatar_url").toString();
    }

}