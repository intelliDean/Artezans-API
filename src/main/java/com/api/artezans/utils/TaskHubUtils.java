package com.api.artezans.utils;

import jakarta.servlet.http.HttpServletRequest;

import java.security.SecureRandom;
import java.util.Base64;

public class TaskHubUtils {
    public static final String VALID_PASSWORD =
            "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@#$%!*?^&+=])[A-Za-z\\d@#$%!*?^&+=]{8,}$";
    public static final String PASSWORD_MESSAGE = "Password must contain at least one lowercase letter, " +
            "one uppercase letter, one special character, and is at least 8 characters long without a white space.";
    public static final int MAX_PER_PAGE = 5;
    public static final String BEARER = "Bearer ";
    public static final String CUSTOMER = "This user is a Customer on Task Hub Platform";
    public static final String SERVICE_PROVIDER = "This user is a Service Provider on Task Hub Platform";

    public static final String VALID_NUMBER = "^(?:\\+61|0)[2-478][0-9]{8}$";
    public static final String NUMBER_MESSAGE = "Please enter a valid phone number";
    public static final String BEFORE_ONE_MONTH = " Account reactivation is one month after deactivation. " +
            "Check back after one month of deactivation";
    public static final String AFTER_SIX_MONTH = " Account cannot be retrieved. Kindly open another account with us.";
    public static final String REACTIVATED = "Account reactivated successfully. Kindly proceed to login.";
    public static final String DEACTIVATED = "You account is deactivated. Activate your account to log in";
    public static final String NOT_NULL = " cannot be null";
    public static final String EMAIL_ERROR_MSG = "Please provide a valid email address";
    public static final String NOT_BLANK = " cannot be blank";
    public static final String NO_LISTINGS = "No listings found";
    public static final String CUSTOMER_NOT_FOUND = "Customer not found";
    public static final String CATEGORY_DOESNT_EXIST = "Category does not exist";
    public static final String TASK_CREATED = "Task created successfully";
    public static final String REJECTED = "Your booking proposal has been rejected. " +
            "Make another proposal or visit another Service Provider";
    public static final String ACCEPTED = "Your booking proposal has been accepted. Please proceed to make payment";

    public static String generateToken(int length) {
        byte[] bytes = new byte[length];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    public static String applicationUrl(HttpServletRequest request) {
        return "https://" + request.getServerName() + ":"
                + request.getServerPort() + request.getContextPath();
    }

    public static String getUrl(String hashString, String token, String url) {
        return "%s?t=%s&e=%s".formatted(url, token, hashString);
    }

    public static String getReactivationUrl(String hashedEmail, String token) {
        return "https://service-rppp.onrender.com/api/v1/user/reactivate" + "?t=" + token + "&e=" + hashedEmail;

    }

    public static String capitalized(String word) {
        String[] words = word.split(" ");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            builder.append(words[i].substring(0, 1).toUpperCase())
                    .append(words[i].substring(1).toLowerCase());
            if (i < words.length - 1) {
                builder.append(" ");
            }
        }
        return builder.toString();
    }
}