package com.student.portfolio.dto;

public record ResetPasswordRequest(String token, String newPassword) {
}