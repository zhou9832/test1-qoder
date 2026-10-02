package com.taskboard.service;

import com.taskboard.dto.TagDto;
import com.taskboard.entity.Tag;
import com.taskboard.repository.TagRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Business logic for Tag management.
 */
@Service
public class TagService {

    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    /**
     * Get all tags ordered by creation time descending.
     */
    @Transactional(readOnly = true)
    public List<TagDto> findAll() {
        return tagRepository.findAllByOrderByCreatedAtDesc().stream()
            .map(this::toDto)
            .toList();
    }

    /**
     * Create a new tag with input validation.
     */
    @Transactional
    public TagDto createTag(String name) {
        validateTagName(name);

        if (tagRepository.existsByName(name.trim())) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Tag already exists: " + name
            );
        }

        Tag tag = new Tag();
        tag.setName(name.trim());

        Tag saved = tagRepository.save(tag);
        return toDto(saved);
    }

    /**
     * Update an existing tag with validation.
     */
    @Transactional
    public TagDto updateTag(Long id, String name) {
        Tag existing = tagRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Tag not found with id: " + id
            ));

        if (name != null && !name.isEmpty()) {
            validateTagName(name);

            if (tagRepository.existsByName(name.trim()) && !name.equals(existing.getName())) {
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Tag already exists: " + name
                );
            }

            existing.setName(name.trim());
        }

        Tag saved = tagRepository.save(existing);
        return toDto(saved);
    }

    /**
     * Delete a tag by ID. Only removes association, does not delete linked tasks.
     */
    @Transactional
    public void deleteTag(Long id) {
        if (!tagRepository.existsById(id)) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Tag not found with id: " + id
            );
        }
        tagRepository.deleteById(id);
    }

    // Private methods

    private void validateTagName(String name) {
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Tag name cannot be blank"
            );
        }
        if (name.length() > 64) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Tag name must be 64 characters or less"
            );
        }
    }

    private TagDto toDto(Tag tag) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
        return new TagDto(
            tag.getId(),
            tag.getName(),
            formatter.format(tag.getCreatedAt()),
            formatter.format(tag.getUpdatedAt())
        );
    }
}
