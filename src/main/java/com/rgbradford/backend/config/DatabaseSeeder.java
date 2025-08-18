package com.rgbradford.backend.config;

import com.rgbradford.backend.entity.Project;
import com.rgbradford.backend.entity.User;
import com.rgbradford.backend.repository.ProjectRepository;
import com.rgbradford.backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

@Configuration
public class DatabaseSeeder {

    @Bean
    @Transactional
    CommandLineRunner seedDatabase(UserRepository userRepository, ProjectRepository projectRepository) {
        return args -> {
            // Seed default project if it doesn't exist
            if (projectRepository.findByName("Default Project").isEmpty()) {
                // Find or create the user only when creating the project
                User defaultUser = userRepository.findByEmail("default@example.com")
                        .orElseGet(() -> {
                            User newUser = new User();
                            newUser.setEmail("default@example.com");
                            newUser.setPassword("password"); // In a real app, hash this!
                            return userRepository.save(newUser);
                        });

                Project defaultProject = new Project();
                defaultProject.setName("Default Project");
                defaultProject.setDescription("This is a default project.");
                defaultProject.setUser(defaultUser);
                projectRepository.save(defaultProject);
            }
        };
    }
}
