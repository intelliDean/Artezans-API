package com.api.artezans.utils;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse(
        Object data,
        String message,
        boolean isSuccessful
) {

    public ApiResponse(Object data, String message, boolean isSuccessful) {
        this.message = message;
        this.data = data;
        this.isSuccessful = isSuccessful;
    }


    public ApiResponse(String message) {
        this(null, message, true);
    }

    public ApiResponse(Object data, String message) {
        this(data, message, true);
    }

    public static ApiResponse apiResponse(Object data, String message) {
        return new ApiResponse(data, message);
    }

    public static ApiResponse apiResponse(String message) {
        return new ApiResponse(message);
    }
}
