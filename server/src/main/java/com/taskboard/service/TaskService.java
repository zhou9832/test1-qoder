package com.taskboard.service;

import com.taskboard.dto.CreateTaskRequest;
import com.taskboard.dto.TaskDto;
import com.taskboard.dto.TagDto;
import com.taskboard.dto.UpdateTaskRequest;
import com.taskboard.entity.Task;
import com.taskboard.entity.Tag;
import com.taskboard.repository.TaskRepository;
import com.taskboard.repository.TagRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Business logic for Task management with state machine validation.
 */
@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final TagRepository tagRepository;

    public TaskService(TaskRepository taskRepository, TagRepository tagRepository) {
        this.taskRepository = taskRepository;
        this.tagRepository = tagRepository;
    }

    /**
     * Get all tasks ordered by creation time descending.
     */
    @Transactional(readOnly = true)
    public List<TaskDto> findAll() {
        return taskRepository.findAllByOrderByCreatedAtDesc().stream()
            .map(this::toDto)
            .toList();
    }

    /**
     * Get tasks by project ID.
     */
    @Transactional(readOnly = true)
    public List<TaskDto> findByProjectId(Long projectId) {
        return taskRepository.findAllByProjectIdOrderByCreatedAtDesc(projectId).stream()
            .map(this::toDto)
            .toList();
    }

    /**
     * Get a task by ID.
     */
    @Transactional(readOnly = true)
    public Optional<TaskDto> findById(Long id) {
        return taskRepository.findById(id).map(this::toDto);
    }

    /**
     * Create a new task with input validation and state machine initial check.
     */
    @Transactional
    public TaskDto createTask(CreateTaskRequest request) {
        validateTitle(request.title());
        validateStatus(request.status());
        validatePriority(request.priority());

        Task task = new Task();
        task.setProjectId(request.projectId());
        task.setTitle(request.title().trim());
        task.setDescription(request.description() != null ? request.description().trim() : "");
        task.setStatus(request.status());
        task.setPriority(request.priority());
        task.setDueAt(parseDueAt(request.dueAt()));

        // Set tags if provided
        if (request.tagIds() != null && !request.tagIds().isEmpty()) {
            List<Tag> tags = tagRepository.findAllById(request.tagIds());
            if (tags.size() != request.tagIds().size()) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, 
                    "One or more tag IDs not found"
                );
            }
            task.getTags().addAll(tags);
        }

        Task saved = taskRepository.save(task);
        return toDto(saved);
    }

    /**
     * Update an existing task with validation.
     */
    @Transactional
    public TaskDto updateTask(Long id, UpdateTaskRequest request) {
        Task existing = taskRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, 
                "Task not found with id: " + id
            ));

        // Validate and update title
        if (request.title() != null && !request.title().isEmpty()) {
            validateTitle(request.title());
            existing.setTitle(request.title().trim());
        }

        // Update description
        if (request.description() != null) {
            String desc = request.description().trim();
            validateDescriptionLength(desc);
            existing.setDescription(desc);
        }

        // Update priority
        if (request.priority() != null) {
            validatePriority(request.priority());
            existing.setPriority(request.priority());
        }

        // Update dueAt
        if (request.dueAt() != null) {
            existing.setDueAt(parseDueAt(request.dueAt()));
        }

        // Update tags if provided
        if (request.tagIds() != null) {
            List<Tag> tags = tagRepository.findAllById(request.tagIds());
            if (tags.size() != request.tagIds().size()) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, 
                    "One or more tag IDs not found"
                );
            }
            existing.getTags().clear();
            existing.getTags().addAll(tags);
        }

        Task saved = taskRepository.save(existing);
        return toDto(saved);
    }

    /**
     * Transition task status using state machine rules.
     * Valid transitions defined in PRD B.4:
     * TODO → IN_PROGRESS
     * IN_PROGRESS → DONE or TODO
     * DONE → CLOSED or IN_PROGRESS
     * CLOSED is terminal state
     */
    @Transactional
    public TaskDto transitionStatus(Long id, String targetStatus) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, 
                "Task not found with id: " + id
            ));

        validateStateTransition(task.getStatus(), targetStatus);
        
        task.setStatus(targetStatus);
        Task saved = taskRepository.save(task);
        return toDto(saved);
    }

    /**
     * Delete a task by ID.
     * Cascade strategy: deletes TimeLog linked to this task, removes tag associations.
     */
    @Transactional
    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND, 
                "Task not found with id: " + id
            );
        }
        taskRepository.deleteById(id);
    }

    /**
     * Check if a task exists by ID.
     */
    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return taskRepository.existsById(id);
    }

    // Private validation methods

    private void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, 
                "Task title cannot be blank"
            );
        }
        if (title.length() > 128) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, 
                "Task title must be 128 characters or less"
            );
        }
    }

    private void validateDescriptionLength(String description) {
        if (description.length() > 1024) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, 
                "Task description must be 1024 characters or less"
            );
        }
    }

    private void validateStatus(String status) {
        if (status != null && !List.of("TODO", "IN_PROGRESS", "DONE", "CLOSED").contains(status)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, 
                "Invalid status: must be TODO, IN_PROGRESS, DONE, or CLOSED"
            );
        }
    }

    private void validatePriority(Integer priority) {
        if (priority == null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, 
                "Priority cannot be null"
            );
        }
        if (priority < 0 || priority > 3) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, 
                "Priority must be between 0 and 3"
            );
        }
    }

    /**
     * Validate state machine transitions per PRD B.4.
     */
    private void validateStateTransition(String currentStatus, String targetStatus) {
        if (currentStatus.equals(targetStatus)) {
            return; // No transition needed
        }

        boolean valid = switch (currentStatus) {
            case "TODO" -> targetStatus.equals("IN_PROGRESS");
            case "IN_PROGRESS" -> targetStatus.equals("DONE") || targetStatus.equals("TODO");
            case "DONE" -> targetStatus.equals("CLOSED") || targetStatus.equals("IN_PROGRESS");
            case "CLOSED" -> false; // Terminal state
            default -> false;
        };

        if (!valid) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Invalid state transition from " + currentStatus + " to " + targetStatus
            );
        }
    }

    private Instant parseDueAt(String dueAt) {
        if (dueAt == null || dueAt.isEmpty()) {
            return null;
        }
        try {
            return DateTimeFormatter.ISO_INSTANT.parse(dueAt, Instant::from);
        } catch (Exception e) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, 
                "Invalid dueAt format: " + dueAt
            );
        }
    }

    /**
     * Convert Entity to DTO with ISO-8601 date formatting and tags mapping.
     */
    private TaskDto toDto(Task task) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
        List<TagDto> tags = task.getTags().stream()
            .map(tag -> new TagDto(
                tag.getId(),
                tag.getName(),
                formatter.format(tag.getCreatedAt()),
                formatter.format(tag.getUpdatedAt())
            ))
            .toList();

        return new TaskDto(
            task.getId(),
            task.getProjectId(),
            task.getTitle(),
            task.getDescription(),
            task.getStatus(),
            task.getPriority(),
            task.getDueAt() != null ? formatter.format(task.getDueAt()) : null,
            tags,
            formatter.format(task.getCreatedAt()),
            formatter.format(task.getUpdatedAt())
        );
    }
}
