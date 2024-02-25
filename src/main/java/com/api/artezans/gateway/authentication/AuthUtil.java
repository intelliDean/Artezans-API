package com.api.artezans.gateway.authentication;

public class AuthUtil {

    public final static String LOGIN_DESCRIPTION = "All users, except admin, logs in using this endpoint by providing " +
            "the email address and password they registered with and get JWT token for authorization";
    public final static String LOGIN_SUMMARY = "User Login";
    public final static String LOGIN_OP_ID = "user.login";

    public final static String LOGOUT_DESCRIPTION = "Service provider, Customer and Admin logs out using this endpoint";
    public final static String LOGOUT_SUMMARY = "All Users Logout";
    public final static String LOGOUT_OP_ID = "user.logout";

    public final static String REFRESH_DESCRIPTION = "When the access token expires, the system automatically " +
            "use the refresh token generate a new access token using this endpoint so that the user can stay logged in";
    public final static String REFRESH_SUMMARY = "Use refresh token to generate new access token";
    public final static String REFRESH_OP_ID = "refresh.generate";
}
