package com.agile.ems.utils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponseDto<T> {
    private int statusCode;
    private String message;
    private boolean error;
    private T data;

    public static <T> ApiResponseDto<T> success(int statusCode, String message, T data) {
        return ApiResponseDto.<T>builder()
                .statusCode(statusCode)
                .message(message)
                .error(false)
                .data(data)
                .build();
    }

    public static <T> ApiResponseDto<T> failure(int statusCode, String message, T data) {
        return ApiResponseDto.<T>builder()
                .statusCode(statusCode)
                .message(message)
                .error(true)
                .data(data)
                .build();
    }
}
