package com.api.artezans.config.Oauth2.userDetail;

import java.util.Map;

public class GoogleOauth2UserData extends Oauth2UserData {

    public GoogleOauth2UserData(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getUserId() {
        Object val = attributes.get("sub");
        return val != null ? val.toString() : null;
    }

    @Override
    public String getName() {
        Object val = attributes.get("name");
        return val != null ? val.toString() : null;
    }

    @Override
    public String getEmail() {
        Object val = attributes.get("email");
        return val != null ? val.toString() : null;
    }

    @Override
    public String getImageUrl() {
        Object val = attributes.get("picture");
        return val != null ? val.toString() : null;
    }
}
