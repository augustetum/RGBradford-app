package com.rgbradford.backend.repository;

import com.rgbradford.backend.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    Optional<Project> findByName(String name);

    //Find all projects for a specific user
    List<Project> findByUserId(Long userId);
}