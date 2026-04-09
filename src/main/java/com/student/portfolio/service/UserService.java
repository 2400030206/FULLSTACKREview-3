package com.student.portfolio.service;

import com.student.portfolio.entity.User;
import com.student.portfolio.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
            throw new IllegalArgumentException("Invalid credentials");
        }

        User current = userOpt.get();
        if (passwordEncoder.matches(password, current.getPassword())) {
            return current;
        }

        // Backward-compatible fallback for older plaintext rows; upgrades them on success.
        if (password.equals(current.getPassword())) {
            current.setPassword(passwordEncoder.encode(password));
            userRepository.save(current);
            logger.info("Upgraded plaintext password hash for username={}", username);
            return current;
        }

        throw new IllegalArgumentException("Invalid credentials");
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
    
    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }
}
