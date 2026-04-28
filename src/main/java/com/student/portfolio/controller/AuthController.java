package com.student.portfolio.controller;

import com.student.portfolio.dto.AuthResponse;
import com.student.portfolio.dto.ForgotPasswordRequest;
import com.student.portfolio.dto.LoginRequest;
import com.student.portfolio.dto.RegisterRequest;
import com.student.portfolio.dto.ResetPasswordRequest;
import com.student.portfolio.dto.UserResponse;
import com.student.portfolio.entity.User;
import com.student.portfolio.security.JwtService;
import com.student.portfolio.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        try {
            User savedUser = userService.registerUser(registerRequest.username(), registerRequest.email(), registerRequest.password(), "ROLE_STUDENT");
            logger.info("Registered new user username={}", savedUser.getUsername());
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Registration successful");
            response.put("user", UserResponse.from(savedUser));
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException ex) {
            logger.warn("Registration failed username={} message={}", registerRequest.username(), ex.getMessage());
            Map<String, String> response = new HashMap<>();
            response.put("error", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (Exception e) {
            logger.error("Unexpected registration error for username={}", registerRequest.username(), e);
            Map<String, String> response = new HashMap<>();
            response.put("error", "Registration failed");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        try {
            User user = userService.authenticate(loginRequest.username(), loginRequest.password());
            String token = jwtService.generateToken(user);
            logger.info("Login successful username={} role={}", user.getUsername(), user.getRole());
            return ResponseEntity.ok(new AuthResponse("Login successful", token, UserResponse.from(user)));
        } catch (IllegalArgumentException ex) {
            logger.warn("Login failed username={} message={}", loginRequest.username(), ex.getMessage());
            Map<String, String> response = new HashMap<>();
            response.put("error", ex.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        } catch (Exception e) {
            logger.error("Unexpected login error for username={}", loginRequest.username(), e);
            Map<String, String> response = new HashMap<>();
            response.put("error", "Login failed");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> sendForgotPasswordEmail(@RequestBody ForgotPasswordRequest request) {
        if (request == null || request.email() == null || request.email().isBlank()) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Email is required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        try {
            userService.processForgotPassword(request.email());
            Map<String, String> response = new HashMap<>();
            response.put("message", "Password reset email sent. Check your inbox.");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Email not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (IllegalStateException ex) {
            Map<String, String> response = new HashMap<>();
            response.put("error", ex.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (Exception ex) {
            logger.error("Unexpected forgot-password error email={}", request.email(), ex);
            Map<String, String> response = new HashMap<>();
            response.put("error", "Unable to process forgot password right now");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        if (request == null || request.token() == null || request.token().isBlank()
                || request.newPassword() == null || request.newPassword().isBlank()) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Token and new password are required");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        boolean updated = userService.resetPassword(request.token(), request.newPassword());
        Map<String, String> response = new HashMap<>();
        if (!updated) {
            response.put("error", "Invalid or expired reset token");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        response.put("message", "Password reset successful");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logged out successfully");
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            User user = userService.findByUsername(authentication.getName()).orElse(null);
            if (user != null) {
                return ResponseEntity.ok(UserResponse.from(user));
            }
        }
        Map<String, String> response = new HashMap<>();
        response.put("error", "Not logged in");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
}
