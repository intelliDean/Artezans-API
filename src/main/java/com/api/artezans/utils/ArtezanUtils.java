package com.api.artezans.utils;

import jakarta.servlet.http.HttpServletRequest;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.stream.Collectors;

/**
 * Application-wide constants and shared utility methods.
 *
 * <p>Constants are grouped by concern:
 * <ul>
 *   <li>Validation — regex patterns and their error messages</li>
 *   <li>Pagination — page-size limits</li>
 *   <li>Auth — bearer prefix, role descriptions</li>
 *   <li>Account lifecycle — messages for activation, deactivation, reactivation</li>
 *   <li>Domain messages — error and success messages used across services</li>
 * </ul>
 */
public final class ArtezanUtils {

    // Prevent instantiation — this is a utility class
    private ArtezanUtils() {
    }

    // -------------------------------------------------------------------------
    // Token generation
    // -------------------------------------------------------------------------

    /**
     * Shared instance — SecureRandom is thread-safe and expensive to construct.
     */
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Generates a URL-safe, Base64-encoded random token of the given byte length.
     * The output string will be approximately {@code ceil(length * 4 / 3)} characters long.
     */
    public static String generateToken(int length) {
        byte[] bytes = new byte[length];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    // -------------------------------------------------------------------------
    // URL building
    // -------------------------------------------------------------------------

    /**
     * Derives the application's base URL from the incoming HTTP request.
     * The scheme is taken from the request to correctly reflect http/https.
     */
    public static String applicationUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String host = request.getServerName();
        int port = request.getServerPort();
        String ctx = request.getContextPath();

        boolean isDefaultPort = ("https".equals(scheme) && port == 443)
                || ("http".equals(scheme) && port == 80);

        return isDefaultPort
                ? scheme + "://" + host + ctx
                : scheme + "://" + host + ":" + port + ctx;
    }

    /**
     * Builds a verification/action URL with a token and hashed email as query parameters.
     *
     * @param baseUrl     the full base URL including path (e.g. {@code https://host/api/v1/user/verify})
     * @param token       the one-time token
     * @param hashedEmail the hashed user email
     */
    public static String buildActionUrl(String baseUrl, String token, String hashedEmail) {
        return "%s?t=%s&e=%s".formatted(baseUrl, token, hashedEmail);
    }

    // -------------------------------------------------------------------------
    // String utilities
    // -------------------------------------------------------------------------

    /**
     * Title-cases every word in a string (first letter upper, rest lower).
     * Handles null, blank strings, and multi-space sequences safely.
     *
     * @param input the raw input string
     * @return the title-cased result, or an empty string if input is null/blank
     */
    public static String capitalized(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }
        return Arrays.stream(input.trim().split("\\s+"))
                .map(word -> word.isEmpty()
                        ? word
                        : Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }

    // -------------------------------------------------------------------------
    // Validation — regex patterns and their error messages
    // -------------------------------------------------------------------------

    /**
     * Regex: at least one letter, one digit, one special character, min 8 chars, no spaces.
     */
    public static final String VALID_PASSWORD =
            "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@#$%!*?^&+=])[A-Za-z\\d@#$%!*?^&+=]{8,}$";

    public static final String PASSWORD_MESSAGE = """
            Password must contain at least one lowercase letter, one uppercase letter,
            one special character, and be at least 8 characters long without spaces.""";

    /**
     * Regex: Nigerian mobile numbers — supports +234 and 0 prefixes.
     */
    public static final String VALID_NUMBER = "^(?:\\+234|0)[7-9][01][0-9]{8}$";
    public static final String NUMBER_MESSAGE = "Please enter a valid Nigerian phone number";

    /**
     * Jakarta Validation message suffix — prepend the field name: {@code "Field" + NOT_NULL}.
     */
    public static final String NOT_NULL = " cannot be null";
    public static final String NOT_BLANK = " cannot be blank";
    public static final String SIZE = " cannot be less than 3 characters";

    public static final String EMAIL_ERROR_MSG = "Please provide a valid email address";

    // -------------------------------------------------------------------------
    // Pagination
    // -------------------------------------------------------------------------

    /**
     * Default maximum number of items returned per page.
     */
    public static final int MAX_PER_PAGE = 5;

    // -------------------------------------------------------------------------
    // Auth
    // -------------------------------------------------------------------------

    public static final String BEARER = "Bearer ";
    public static final String CUSTOMER = "This user is a customer on the Artezan platform";
    public static final String SERVICE_PROVIDER = "This user is a service provider on the Artezan platform";

    // -------------------------------------------------------------------------
    // Account lifecycle messages
    // -------------------------------------------------------------------------

    public static final String DEACTIVATED =
            "Your account is deactivated. Please activate your account to log in.";

    public static final String REACTIVATED =
            "Account reactivated successfully. Kindly proceed to login.";

    public static final String BEFORE_ONE_MONTH = """
            Account reactivation is only available from one month after deactivation.
            Please check back later.""";

    public static final String AFTER_SIX_MONTH =
            "Account cannot be retrieved after six months. Please open a new account with us.";

    public static final String TOKEN_EXPIRED =
            "Your verification token has expired. A new verification email has been sent to your address.";

    /**
     * Returns a message telling the user how many minutes remain on their token.
     * Use instead of the raw format string to make the intent explicit at call sites.
     */
    public static String tokenStillValidMessage(long minutesRemaining) {
        return "Your verification token is still valid for the next %d minute(s). "
                .formatted(minutesRemaining) +
                "Check your registered email to verify your address.";
    }

    // -------------------------------------------------------------------------
    // Domain messages
    // -------------------------------------------------------------------------

    public static final String NO_LISTINGS = "No listings found";
    public static final String CUSTOMER_NOT_FOUND = "Customer not found";
    public static final String CATEGORY_DOESNT_EXIST = "Category does not exist";
    public static final String TASK_CREATED = "Task created successfully";

    public static final String ACCEPTED = """
            Your booking proposal has been accepted. Please proceed to make payment.""";

    public static final String REJECTED = """
            Your booking proposal has been rejected.
            You may make another proposal or visit a different service provider.""";
}