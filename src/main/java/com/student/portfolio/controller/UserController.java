package com.student.portfolio.controller;

import com.student.portfolio.dto.PasswordUpdateRequest;
import com.student.portfolio.dto.RegisterRequest;
import com.student.portfolio.dto.UserLoginRequest;
import com.student.portfolio.dto.UserResponse;
import com.student.portfolio.entity.User;
import com.student.portfolio.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            User savedUser = userService.registerUser(
                    request.username(),
                    request.email(),
                    request.password(),
                    "ROLE_STUDENT"
            );

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Registration successful");
            response.put("user", UserResponse.from(savedUser));
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            Map<String, String> response = new HashMap<>();
            response.put("error", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginRequest request) {
        try {
            User user = userService.loginWithEmail(request.email(), request.password());
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Login successful");
            response.put("user", UserResponse.from(user));
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Invalid credentials");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @PutMapping("/users/{id}/password")
    public ResponseEntity<?> updatePassword(@PathVariable Long id, @RequestBody PasswordUpdateRequest request) {
        if (request == null || request.password() == null || request.password().isBlank()) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Password must not be empty");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        boolean updated = userService.updatePasswordById(id, request.password());
        if (!updated) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "User not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Map<String, String> response = new HashMap<>();
        response.put("message", "Password updated successfully");
        return ResponseEntity.ok(response);
    }
}