package com.student.portfolio.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @Column(nullable = false)
    private String role; // "ROLE_STUDENT" or "ROLE_ADMIN"

    // One user can have many projects
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Project> projects;

    // One user has one portfolio profile
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Portfolio portfolio;

    // Reset Token for password reset
    @Column(name = "reset_token")
    private String resetToken;

    @Column(name = "token_expiry")
    private java.time.LocalDateTime tokenExpiry;

    // Constructors
    public User() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public List<Project> getProjects() { return projects; }
    public void setProjects(List<Project> projects) { this.projects = projects; }

    public Portfolio getPortfolio() { return portfolio; }
    public void setPortfolio(Portfolio portfolio) { this.portfolio = portfolio; }

    public String getResetToken() { return resetToken; }
    public void setResetToken(String resetToken) { this.resetToken = resetToken; }

    public java.time.LocalDateTime getTokenExpiry() { return tokenExpiry; }
    public void setTokenExpiry(java.time.LocalDateTime tokenExpiry) { this.tokenExpiry = tokenExpiry; }
}
