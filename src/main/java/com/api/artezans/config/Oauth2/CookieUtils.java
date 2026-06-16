package com.api.artezans.config.Oauth2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * Utility helpers for reading, writing, and deleting HTTP cookies.
 * Serialization uses Jackson (JSON) instead of Java Serialization to avoid
 * the deserialization security vulnerability in spring SerializationUtils.
 */
@Slf4j
public class CookieUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return Optional.of(cookie);
                }
            }
        }
        return Optional.empty();
    }

    public static void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    cookie.setValue("");
                    cookie.setPath("/");
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);
                }
            }
        }
    }

    public static String serialize(Object object) {
        try {
            return MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize cookie value", e);
            return "";
        }
    }

    public static <T> T deserialize(Cookie cookie, Class<T> cls) {
        try {
            return MAPPER.readValue(cookie.getValue(), cls);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize cookie value for class {}", cls.getSimpleName(), e);
            return null;
        }
    }
}
