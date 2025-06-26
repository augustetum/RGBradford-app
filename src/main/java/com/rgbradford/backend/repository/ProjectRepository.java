package com.rgbradford.backend.repository;

import com.rgbradford.backend.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    
    //Find all projects for a specific user
    List<Project> findByUserId(Long userId);
    
    //Find projects by name (case-insensitive)
    List<Project> findByNameContainingIgnoreCase(String name);
    
    //Find projects by user ID and order by creation date (newest first)
    List<Project> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    //Find projects created after a specific date
    List<Project> findByCreatedAtAfter(java.time.LocalDateTime date);
} 