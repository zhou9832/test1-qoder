package com.taskboard.controller;

import com.taskboard.dto.ApiResponse;
import com.taskboard.dto.TagDto;
import com.taskboard.service.TagService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Tag management.
 */
@RestController
@RequestMapping("/api/tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    /**
     * Get all tags.
     */
    @GetMapping
    public ApiResponse<List<TagDto>> getAllTags() {
        List<TagDto> tags = tagService.findAll();
        return ApiResponse.success(tags);
    }

    /**
     * Create a new tag.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<TagDto> createTag(@RequestParam String name) {
        TagDto created = tagService.createTag(name);
        return ApiResponse.success(created);
    }

    /**
     * Update an existing tag.
     */
    @PutMapping("/{id}")
    public ApiResponse<TagDto> updateTag(
            @PathVariable Long id,
            @RequestParam(required = false) String name
    ) {
        TagDto updated = tagService.updateTag(id, name);
        return ApiResponse.success(updated);
    }

    /**
     * Delete a tag by ID.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> deleteTag(@PathVariable Long id) {
        tagService.deleteTag(id);
        return ApiResponse.success(null);
    }
}
