package com.taskboard.repository;

import com.taskboard.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository for Tag entity.
 */
public interface TagRepository extends JpaRepository<Tag, Long> {

    /**
     * Find all tags ordered by creation time descending.
     */
    List<Tag> findAllByOrderByCreatedAtDesc();

    /**
     * Check if a tag with the given name already exists.
     */
    boolean existsByName(String name);
}
