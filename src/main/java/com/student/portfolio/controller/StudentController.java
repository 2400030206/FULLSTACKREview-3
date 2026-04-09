package com.student.portfolio.controller;

import com.student.portfolio.entity.Milestone;
import com.student.portfolio.entity.Portfolio;
import com.student.portfolio.entity.Project;
import com.student.portfolio.entity.User;
import com.student.portfolio.service.MilestoneService;
import com.student.portfolio.service.PortfolioService;
import com.student.portfolio.service.ProjectService;
import com.student.portfolio.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    private static final Logger logger = LoggerFactory.getLogger(StudentController.class);

    @Autowired
    private ProjectService projectService;

    @Autowired
    private MilestoneService milestoneService;

    @Autowired
    private PortfolioService portfolioService;

    @Autowired
    private UserService userService;

    // Helper method to check if the authenticated user is a student
    private User getAuthenticatedStudent(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        if (authentication.getAuthorities().stream().noneMatch(authority -> "ROLE_STUDENT".equals(authority.getAuthority()))) {
            return null;
        }

        return userService.findByUsername(authentication.getName()).orElse(null);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard(Authentication authentication) {
        User user = getAuthenticatedStudent(authentication);
        if (user == null) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Unauthorized");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(err);
        }
        logger.debug("Student dashboard accessed username={}", user.getUsername());
        
        List<Project> projects = projectService.getProjectsByUser(user);
        Map<String, Object> response = new HashMap<>();
        response.put("user", user);
        response.put("projectCount", projects.size());
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/projects")
    public ResponseEntity<?> createProject(@RequestBody Project project, Authentication authentication) {
        User user = getAuthenticatedStudent(authentication);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }
        
        project.setUser(user);
        project.setStatus("Pending"); // Automatically pending for admin approval
        Project savedProject = projectService.saveProject(project);
        
        return ResponseEntity.ok(savedProject);
    }

    @GetMapping("/projects")
    public ResponseEntity<?> listProjects(Authentication authentication) {
        User user = getAuthenticatedStudent(authentication);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }
        
        List<Project> projects = projectService.getProjectsByUser(user);
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/projects/{id}")
    public ResponseEntity<?> getProjectWithMilestones(@PathVariable Long id, Authentication authentication) {
        User user = getAuthenticatedStudent(authentication);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }
        
        Project project = projectService.getProjectById(id);
        // Ensure that a student can only view their own projects
        if (project == null || !project.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access denied"));
        }
        
        List<Milestone> milestones = milestoneService.getMilestonesByProject(project);
        
        Map<String, Object> response = new HashMap<>();
        response.put("project", project);
        response.put("milestones", milestones);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/projects/{id}/milestones")
    public ResponseEntity<?> addMilestone(@PathVariable Long id, @RequestBody Milestone milestone, Authentication authentication) {
        User user = getAuthenticatedStudent(authentication);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }
        
        Project project = projectService.getProjectById(id);
        if (project != null && project.getUser().getId().equals(user.getId())) {
            milestone.setProject(project);
            Milestone savedMilestone = milestoneService.saveMilestone(milestone);
            return ResponseEntity.ok(savedMilestone);
        }
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Access denied"));
    }

    @GetMapping("/portfolio")
    public ResponseEntity<?> getPortfolio(Authentication authentication) {
        User user = getAuthenticatedStudent(authentication);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }
        
        Portfolio portfolio = portfolioService.getPortfolioByUser(user);
        if (portfolio == null) {
            portfolio = new Portfolio();
            portfolio.setUser(user);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("portfolio", portfolio);
        response.put("user", user);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/portfolio")
    public ResponseEntity<?> savePortfolio(@RequestBody Portfolio portfolioData, Authentication authentication) {
        User user = getAuthenticatedStudent(authentication);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Unauthorized"));
        }
        
        Portfolio existing = portfolioService.getPortfolioByUser(user);
        if (existing != null) {
            existing.setSkills(portfolioData.getSkills());
            existing.setAchievements(portfolioData.getAchievements());
            Portfolio updated = portfolioService.savePortfolio(existing);
            return ResponseEntity.ok(updated);
        } else {
            portfolioData.setUser(user);
            Portfolio saved = portfolioService.savePortfolio(portfolioData);
            return ResponseEntity.ok(saved);
        }
    }
}
