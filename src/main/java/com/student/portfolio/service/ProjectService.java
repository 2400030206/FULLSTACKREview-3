package com.student.portfolio.service;

import com.student.portfolio.entity.Project;
import com.student.portfolio.entity.User;
import com.student.portfolio.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    public Project saveProject(Project project) {
        return projectRepository.save(project);
    }

    public List<Project> getProjectsByUser(User user) {
        return projectRepository.findByUser(user);
    }

    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    public Project getProjectById(Long id) {
        return projectRepository.findById(id).orElse(null);
    }

    public void updateProjectStatusAndFeedback(Long projectId, String status, String feedback) {
        Optional<Project> opt = projectRepository.findById(projectId);
        if (opt.isPresent()) {
            Project p = opt.get();
            p.setStatus(status);
            p.setFeedback(feedback);
            projectRepository.save(p);
        }
    }
}
