package com.student.portfolio.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "portfolios")
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // One portfolio belongs to one user
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Lob
    private String skills; // Comma separated list of skills

    @Lob
    private String achievements; // Details about achievements

    // Constructors
    public Portfolio() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }

    public String getAchievements() { return achievements; }
    public void setAchievements(String achievements) { this.achievements = achievements; }
}
