package com.taskboard.repository;

import com.taskboard.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for Task entity.
 */
public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * Find all tasks ordered by creation time descending.
     */
    List<Task> findAllByOrderByCreatedAtDesc();

    /**
     * Find tasks by project ID.
     */
    List<Task> findAllByProjectIdOrderByCreatedAtDesc(Long projectId);

    /**
     * Check if a task exists by ID.
     */
    boolean existsById(Long id);
}
