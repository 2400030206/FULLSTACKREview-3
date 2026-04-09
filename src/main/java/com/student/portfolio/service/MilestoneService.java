package com.student.portfolio.service;

import com.student.portfolio.entity.Milestone;
import com.student.portfolio.entity.Project;
import com.student.portfolio.repository.MilestoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MilestoneService {

    @Autowired
    private MilestoneRepository milestoneRepository;

    public Milestone saveMilestone(Milestone milestone) {
        return milestoneRepository.save(milestone);
    }

    public List<Milestone> getMilestonesByProject(Project project) {
        return milestoneRepository.findByProject(project);
    }
    
    public void deleteMilestone(Long id) {
        milestoneRepository.deleteById(id);
    }
}
