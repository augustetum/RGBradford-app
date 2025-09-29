package com.rgbradford.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Entity
@Table(name = "projects")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    //Path to the picture of the plate (stored in the user's phone locally)
    @Column(name = "picture_file_path")
    private String pictureFilePath;

    //Reference to the user who owns the project
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    //One project has one plate
    @OneToOne(mappedBy = "project", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH}, orphanRemoval = true, fetch = FetchType.LAZY)
    private PlateLayout plateLayout;
    
    //Helper method to set the plate layout and maintain the bidirectional relationship
    public void setPlateLayout(PlateLayout plateLayout) {
        if (plateLayout == null) {
            if (this.plateLayout != null) {
                PlateLayout existingPlateLayout = this.plateLayout;
                this.plateLayout = null;
                existingPlateLayout.setProjectInternal(null);
            }
        } else {
            plateLayout.setProjectInternal(this);
        }
        this.plateLayout = plateLayout;
    }
    
    //Internal method to set the project without causing an infinite loop
    void setPlateLayoutInternal(PlateLayout plateLayout) {
        this.plateLayout = plateLayout;
    }

    //PrePersist is used for timestamping the creation of the project BEFORE it is saved to the database
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    //PreUpdate is used for timestamping the update of the project AFTER it is saved to the database
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
} 