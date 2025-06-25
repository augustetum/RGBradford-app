package com.rgbradford.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;
import java.util.List;

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

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    //One project can have many plate layouts (or plates, in other words)
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlateLayout> plateLayouts;

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