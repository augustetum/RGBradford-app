package com.rgbradford.backend.repository;

import com.rgbradford.backend.entity.Well;
import com.rgbradford.backend.entity.WellType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    
    //Paged methods for filtering
    Page<Well> findByPlateLayoutId(Long plateLayoutId, Pageable pageable);
    Page<Well> findByType(WellType type, Pageable pageable);
    Page<Well> findByReplicateGroup(String replicateGroup, Pageable pageable);
    
    //Delete wells by plate layout ID
    void deleteByPlateLayoutId(Long plateLayoutId);

    /**
     * Bulk update well types by IDs
     */
    @Modifying
    @Query("UPDATE Well w SET w.type = :wellType WHERE w.id IN :wellIds")
    void updateWellTypesByIds(@Param("wellIds") List<Long> wellIds, @Param("wellType") WellType wellType);

    /**
     * Single bulk update using SQL Server CASE statement - most efficient option
     */
    @Modifying
    @Query(value = """
        UPDATE wells 
        SET type = CASE 
            WHEN id IN (:standardIds) THEN 'STANDARD'
            WHEN id IN (:sampleIds) THEN 'SAMPLE' 
            WHEN id IN (:blankIds) THEN 'BLANK'
            ELSE 'EMPTY'
        END
        WHERE plate_layout_id = :plateLayoutId
        """, nativeQuery = true)
    void bulkUpdateWellTypes(@Param("plateLayoutId") Long plateLayoutId,
                             @Param("standardIds") List<Long> standardIds,
                             @Param("sampleIds") List<Long> sampleIds,
                             @Param("blankIds") List<Long> blankIds);

} 