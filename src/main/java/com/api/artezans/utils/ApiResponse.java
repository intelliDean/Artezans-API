package com.api.artezans.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse {

    private Object data;

    private String message;

    private boolean isSuccessful;

    public ApiResponse(String message) {
        this.message = message;
        this.isSuccessful = true;
    }

    public ApiResponse(Object data, String message) {
        this(message);
        this.data = data;
    }

    public static ApiResponse apiResponse(Object data, String message) {
        return new ApiResponse(data, message);
    }

    public static ApiResponse apiResponse(String message) {
        return new ApiResponse(message);
    }
}
