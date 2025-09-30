package com.rgbradford.backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Project> projects = new ArrayList<>();
    
    //Helper method to add project and set the bidirectional relationship
    public void addProject(Project project) {
        projects.add(project);
        project.setUser(this);
    }
    
    //Helper method to remove project and clear the bidirectional relationship
    public void removeProject(Project project) {
        projects.remove(project);
        project.setUser(null);
    }
} 