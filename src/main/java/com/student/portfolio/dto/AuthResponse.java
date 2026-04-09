package com.student.portfolio.dto;

public record AuthResponse(String message, String token, UserResponse user) {
}