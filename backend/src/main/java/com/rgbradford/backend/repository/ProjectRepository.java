package com.rgbradford.backend.repository;

import com.rgbradford.backend.entity.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    Optional<Project> findByName(String name);

    // Find a project by ID and user email
    @Query("SELECT p FROM Project p JOIN p.user u WHERE p.id = :id AND u.email = :email")
    Optional<Project> findByIdAndUser_Email(@Param("id") Long id, @Param("email") String email);
    
    // Find all projects for a specific user with pagination
    @Query("SELECT p FROM Project p WHERE p.user.id = :userId")
    Page<Project> findByUserId(@Param("userId") Long userId, Pageable pageable);
    
    // Keep the non-paginated version for backward compatibility
    @Query("SELECT p FROM Project p WHERE p.user.id = :userId")
    List<Project> findAllByUserId(@Param("userId") Long userId);
}