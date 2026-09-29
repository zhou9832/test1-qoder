package com.taskboard.repository;

import com.taskboard.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Project entity operations.
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    /**
     * Find all projects ordered by creation time descending.
     */
    List<Project> findAllByOrderByCreatedAtDesc();

    /**
     * Check if a project with the given name already exists.
     */
    boolean existsByName(String name);
}
