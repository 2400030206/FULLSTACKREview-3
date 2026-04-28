package com.student.portfolio.service;

import com.student.portfolio.entity.User;
import com.student.portfolio.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Value("${app.password-reset.token-expiration-minutes:15}")
    private long tokenExpirationMinutes;

    public User registerUser(String username, String email, String rawPassword, String role) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role == null || role.isBlank() ? "ROLE_STUDENT" : role);

        logger.info("Registering user username={} role={}", username, user.getRole());
        return userRepository.save(user);
    }

    public User authenticate(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByEmail(username);
        }
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        User current = userOpt.get();
        if (passwordEncoder.matches(password, current.getPassword())) {
            return current;
        }

        throw new IllegalArgumentException("Invalid credentials");
    }

    public User loginWithEmail(String email, String rawPassword) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        return user;
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean updatePasswordByEmail(String email, String newPassword) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            logger.info("Password updated for email={}", email);
            return true;
        }
        return false;
    }

    public void processForgotPassword(String email) {
        logger.info("Processing forgot password request for email: {}", email);
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            logger.warn("Forgot password request failed: email not found - {}", email);
            throw new IllegalArgumentException("Email not found");
        }

        try {
            User user = userOpt.get();
            String token = java.util.UUID.randomUUID().toString();
            user.setResetToken(token);
            user.setTokenExpiry(java.time.LocalDateTime.now().plusMinutes(tokenExpirationMinutes));
            userRepository.save(user);
            logger.info("Reset token generated for user: {}", email);

            logger.info("Sending reset email to: {}", email);
            emailService.sendResetEmail(user.getEmail(), token, tokenExpirationMinutes);
            logger.info("Forgot password email process completed successfully for: {}", email);
        } catch (Exception ex) {
            logger.error("Error in processForgotPassword for email: {} - {}", email, ex.getMessage());
            logger.error("Full exception:", ex);
            throw ex;
        }
    }

    public boolean resetPassword(String token, String newPassword) {
        Optional<User> userOpt = userRepository.findByResetToken(token);
        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();
        if (user.getTokenExpiry() == null || user.getTokenExpiry().isBefore(java.time.LocalDateTime.now())) {
            return false;
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setTokenExpiry(null);
        userRepository.save(user);
        
        logger.info("Password successfully reset for user id={}", user.getId());
        return true;
    }

    public boolean updatePasswordById(Long id, String rawPassword) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return false;
        }

        String encodedPassword = passwordEncoder.encode(rawPassword);
        int updatedRows = userRepository.updatePasswordById(id, encodedPassword);
        if (updatedRows > 0) {
            logger.info("Password updated for userId={}", id);
            return true;
        }

        return false;
    }

    public int migratePlaintextPasswords() {
        List<User> users = userRepository.findAll();
        int migratedCount = 0;

        for (User user : users) {
            String currentPassword = user.getPassword();
            if (currentPassword == null || currentPassword.isBlank()) {
                continue;
            }

            if (!isBcryptHash(currentPassword)) {
                user.setPassword(passwordEncoder.encode(currentPassword));
                migratedCount++;
            }
        }

        if (migratedCount > 0) {
            userRepository.saveAll(users);
            logger.info("Migrated {} plaintext password(s) to BCrypt", migratedCount);
        }

        return migratedCount;
    }

    public boolean verifyPasswordByEmail(String email, String rawPassword) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return false;
        }

        String storedPassword = userOpt.get().getPassword();
        if (storedPassword == null || storedPassword.isBlank()) {
            return false;
        }

        return isBcryptHash(storedPassword) && passwordEncoder.matches(rawPassword, storedPassword);
    }

    private boolean isBcryptHash(String password) {
        return password.length() == 60
                && (password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$"));
    }
    
    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }
}
