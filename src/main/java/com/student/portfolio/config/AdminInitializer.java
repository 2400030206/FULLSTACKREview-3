package com.student.portfolio.config;

import com.student.portfolio.entity.User;
import com.student.portfolio.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AdminInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private com.student.portfolio.service.UserService userService;

    @Override
    public void run(String... args) throws Exception {
        // Run the password migration on startup
        int count = userService.migratePlaintextPasswords();
        if (count > 0) {
            System.out.println("===============================================");
            System.out.println("Migrated " + count + " plaintext passwords to BCrypt!");
            System.out.println("===============================================");
        }

        Optional<User> adminOpt = userRepository.findByUsername("admin");
        if (adminOpt.isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setEmail("admin@college.edu");
            admin.setRole("ROLE_ADMIN");
            userRepository.save(admin);
            System.out.println("===============================================");
            System.out.println("Default Admin Account Created!");
            System.out.println("Username: admin");
            System.out.println("Password: admin123");
            System.out.println("===============================================");
        }
    }
}
