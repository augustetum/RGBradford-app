package com.rgbradford.backend.repository;

import com.rgbradford.backend.entity.Well;
import com.rgbradford.backend.entity.WellType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface WellRepository extends JpaRepository<Well, Long> {
    
    //Find all wells for a specific plate layout
    List<Well> findByPlateLayoutId(Long plateLayoutId);
    
    //Find wells by plate layout ID and order by position
    List<Well> findByPlateLayoutIdOrderByRowAscColumnAsc(Long plateLayoutId);
    
    //Find wells by type within a plate layout
    List<Well> findByPlateLayoutIdAndType(Long plateLayoutId, WellType type);
    
    //Find standard wells for a plate layout (for calibration)
    List<Well> findByPlateLayoutIdAndTypeOrderByStandardConcentrationAsc(Long plateLayoutId, WellType type);
    
    //Find sample wells for a plate layout
    List<Well> findByPlateLayoutIdAndTypeOrderBySampleNameAsc(Long plateLayoutId, WellType type);
    
    //Find well by position in a plate layout
    Optional<Well> findByPlateLayoutIdAndRowAndColumn(Long plateLayoutId, int row, int column);
    
    //Find wells by sample name
    List<Well> findBySampleNameContainingIgnoreCase(String sampleName);
    
    //Find wells by replicate group
    List<Well> findByReplicateGroup(String replicateGroup);
    
    //Find wells with analysis data (has WellAnalysis)
    List<Well> findByWellAnalysisIsNotNull();
} 