package com.api.artezans.config.utils;

public class NoAuth {
    public static String[] whiteList() {
        return new String[]{
                "/api/v1/customer/sign-up",
                "/api/v1/service_provider/sign-up",
                "/api/v1/customer/verify",
                "/api/v1/service_provider/verify",
                "/api/v1/admin/login",
                "/api/v1/customer/login",
                "/api/v1/service_provider/login",
                "/api/v1/customer/complete",
                "/api/v1/user/**",
                "/api/v1/service_provider/complete",
                "api/v1/change-password/change",
                "/api/v1/auth/**",
                "api/v1/booking/stripe-webhook"
        };
    }


    public static String[] swagger() {
        return new String[]{
                "/swagger-ui.html",
                "/swagger-ui/**",
                "/v3/api-docs",
                "/v3/api-docs/**"
        };
    }
}