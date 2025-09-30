package com.rgbradford.backend.repository;

import com.rgbradford.backend.entity.PlateLayout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PlateLayoutRepository extends JpaRepository<PlateLayout, Long> {
    
    //Find all plate layouts for a specific project
    List<PlateLayout> findByProjectId(Long projectId);
    
    //Find plate layouts by project ID and order by creation date (newest first)
    List<PlateLayout> findByProjectIdOrderByIdDesc(Long projectId);
    
    //Find plate layouts by dimensions
    List<PlateLayout> findByRowsAndColumns(int rows, int columns);
} 