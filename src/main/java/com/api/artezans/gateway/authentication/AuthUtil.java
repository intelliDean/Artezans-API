package com.api.artezans.gateway.authentication;

public interface AuthUtil {

    // Login
    String LOGIN_SUMMARY = "User Login";
    String LOGIN_DESCRIPTION = """
            All users, except admin, log in using this endpoint by providing
            the email address and password they registered with and receive a JWT token for authorization.
            """;
    String LOGIN_OP_ID = "user.login";

    // Logout
    String LOGOUT_SUMMARY = "All Users Logout";
    String LOGOUT_DESCRIPTION = "Service provider, customer, and admin log out using this endpoint.";
    String LOGOUT_OP_ID = "user.logout";

    // Refresh token
    String REFRESH_SUMMARY = "Generate new access token";
    String REFRESH_DESCRIPTION = """
            When the access token expires, the system automatically uses the refresh token
            to generate a new access token via this endpoint so the user can remain logged in.
            """;
    String REFRESH_OP_ID = "refresh.generate";
}
