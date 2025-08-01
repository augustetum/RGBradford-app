package com.rgbradford.backend.repository;

import com.rgbradford.backend.entity.WellAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface WellAnalysisRepository extends JpaRepository<WellAnalysis, Long> {
    
    //Find analysis by well ID
    Optional<WellAnalysis> findByWellId(Long wellId);
    
    //Find analyses with calculated concentration greater than a value
    List<WellAnalysis> findByCalculatedConcentrationGreaterThan(Double concentration);
    
    //Find analyses with calculated concentration less than a value
    List<WellAnalysis> findByCalculatedConcentrationLessThan(Double concentration);
    
    //Find analyses by blue-green ratio range
    List<WellAnalysis> findByBlueGreenRatioBetween(Double minRatio, Double maxRatio);
    
    //Find analyses with high blue values (indicating protein presence)
    List<WellAnalysis> findByBlueValueGreaterThan(Integer blueValue);
    
    //Find analyses ordered by calculated concentration (highest first)
    List<WellAnalysis> findAllByOrderByCalculatedConcentrationDesc();
    
    //Find analyses with calculated concentration not null
    List<WellAnalysis> findByCalculatedConcentrationIsNotNull();

    @Query("SELECT wa FROM WellAnalysis wa WHERE wa.well.plateLayout.id = :plateLayoutId")
    List<WellAnalysis> findByPlateLayoutId(@Param("plateLayoutId") Long plateLayoutId);
} 