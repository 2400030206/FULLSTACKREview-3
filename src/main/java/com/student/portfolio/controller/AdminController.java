package com.student.portfolio.controller;

import com.student.portfolio.entity.Project;
import com.student.portfolio.entity.User;
import com.student.portfolio.service.ProjectService;
import com.student.portfolio.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserService userService;

    // Helper method to ensure session belongs to an admin
    private User getAdminSessionUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        if (authentication.getAuthorities().stream().noneMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()))) {
            return null;
        }

        return userService.findByUsername(authentication.getName()).orElse(null);
    }

    @GetMapping("/projects")
    public ResponseEntity<?> getAllProjects(Authentication authentication) {
        if (getAdminSessionUser(authentication) == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized admins only"));
        }

        logger.debug("Admin project list accessed");
        List<Project> allProjects = projectService.getAllProjects();
        return ResponseEntity.ok(allProjects);
    }

    @PostMapping("/projects/{id}/review")
    public ResponseEntity<?> reviewProject(@PathVariable Long id, 
                                           @RequestBody Map<String, String> reviewData, 
                                           Authentication authentication) {
        if (getAdminSessionUser(authentication) == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized admins only"));
        }

        String status = reviewData.get("status");
        String feedback = reviewData.get("feedback");

        logger.info("Admin reviewing project id={} status={}", id, status);
        projectService.updateProjectStatusAndFeedback(id, status, feedback);
        
        Project updatedProject = projectService.getProjectById(id);
        return ResponseEntity.ok(updatedProject);
    }

    @PostMapping("/users/migrate-passwords")
    public ResponseEntity<?> migrateUserPasswords(Authentication authentication) {
        if (getAdminSessionUser(authentication) == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized admins only"));
        }

        int migratedUsers = userService.migratePlaintextPasswords();
        return ResponseEntity.ok(Map.of(
                "message", "Password migration completed",
                "migratedUsers", migratedUsers
        ));
    }

    @PostMapping("/users/verify-password")
    public ResponseEntity<?> verifyUserPassword(@RequestBody Map<String, String> request, Authentication authentication) {
        if (getAdminSessionUser(authentication) == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized admins only"));
        }

        String email = request.get("email");
        String rawPassword = request.get("password");
        if (email == null || email.isBlank() || rawPassword == null || rawPassword.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "email and password are required"));
        }

        boolean matches = userService.verifyPasswordByEmail(email, rawPassword);
        return ResponseEntity.ok(Map.of("matches", matches));
    }

    @PostMapping("/users/reset-password")
    public ResponseEntity<?> resetUserPassword(@RequestBody Map<String, String> request, Authentication authentication) {
        if (getAdminSessionUser(authentication) == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized admins only"));
        }

        String email = request.get("email");
        String newPassword = request.get("newPassword");
        if (email == null || email.isBlank() || newPassword == null || newPassword.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "email and newPassword are required"));
        }

        boolean updated = userService.updatePasswordByEmail(email, newPassword);
        if (!updated) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "User not found"));
        }

        return ResponseEntity.ok(Map.of("message", "Password reset successful"));
    }
}
