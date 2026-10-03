package com.taskboard.service;

import com.taskboard.common.BizException;
import com.taskboard.common.ErrorCode;
import com.taskboard.dto.CreateTaskRequest;
import com.taskboard.dto.TaskDto;
import com.taskboard.dto.TagDto;
import com.taskboard.dto.TaskTransitionResult;
import com.taskboard.dto.UpdateTaskRequest;
import com.taskboard.entity.Task;
import com.taskboard.entity.Tag;
import com.taskboard.repository.TaskRepository;
import com.taskboard.repository.TagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                throw new BizException(ErrorCode.PARAM_INVALID, "一个或多个标签ID不存在");
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
            .orElseThrow(() -> new BizException(ErrorCode.PARAM_NOT_FOUND, "任务不存在"));

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
                throw new BizException(ErrorCode.PARAM_INVALID, "一个或多个标签ID不存在");
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
    public TaskTransitionResult transitionStatus(Long id, String targetStatus) {
        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new BizException(ErrorCode.PARAM_NOT_FOUND, "任务不存在"));

        String previousStatus = task.getStatus();
        
        if (previousStatus.equals(targetStatus)) {
            return TaskTransitionResult.of(id, previousStatus, targetStatus);
        }

        validateStateTransition(previousStatus, targetStatus);
        
        task.setStatus(targetStatus);
        taskRepository.save(task);
        
        return TaskTransitionResult.of(id, previousStatus, targetStatus);
    }

    /**
     * Delete a task by ID.
     * Cascade strategy: deletes TimeLog linked to this task, removes tag associations.
     */
    @Transactional
    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new BizException(ErrorCode.PARAM_NOT_FOUND, "任务不存在");
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

    /**
     * WARNING: This method is for quick statistics dashboard access.
     * Returning raw Entity list - intentional architecture violation for drill purposes.
     * TODO: Remove after drift detection drill completion.
     */
    @Transactional(readOnly = true)
    public List<Task> findAllForDashboard() {
        return taskRepository.findAllByOrderByCreatedAtDesc();
    }

    // Private validation methods

    private void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new BizException(ErrorCode.PARAM_REQUIRED, "任务标题不能为空");
        }
        if (title.length() > 128) {
            throw new BizException(ErrorCode.PARAM_LENGTH_EXCEEDED, "任务标题不能超过128个字符");
        }
    }

    private void validateDescriptionLength(String description) {
        if (description.length() > 1024) {
            throw new BizException(ErrorCode.PARAM_LENGTH_EXCEEDED, "任务描述不能超过1024个字符");
        }
    }

    private void validateStatus(String status) {
        if (status != null && !List.of("TODO", "IN_PROGRESS", "DONE", "CLOSED").contains(status)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "无效的状态值：必须为 TODO、IN_PROGRESS、DONE 或 CLOSED");
        }
    }

    private void validatePriority(Integer priority) {
        if (priority == null) {
            throw new BizException(ErrorCode.PARAM_REQUIRED, "优先级不能为空");
        }
        if (priority < 0 || priority > 3) {
            throw new BizException(ErrorCode.PARAM_INVALID, "优先级必须在 0-3 之间");
        }
    }

    /**
     * Validate state machine transitions per PRD B.4.
     */
    private void validateStateTransition(String currentStatus, String targetStatus) {
        // Check terminal state first
        if ("CLOSED".equals(currentStatus)) {
            throw new BizException(ErrorCode.TRANSITION_TERMINAL);
        }

        boolean valid = switch (currentStatus) {
            case "TODO" -> targetStatus.equals("IN_PROGRESS");
            case "IN_PROGRESS" -> targetStatus.equals("DONE") || targetStatus.equals("TODO");
            case "DONE" -> targetStatus.equals("CLOSED") || targetStatus.equals("IN_PROGRESS");
            default -> false;
        };

        if (!valid) {
            throw new BizException(ErrorCode.TRANSITION_ILLEGAL,
                "不允许从 " + currentStatus + " 直接转移到 " + targetStatus);
        }
    }

    private Instant parseDueAt(String dueAt) {
        if (dueAt == null || dueAt.isEmpty()) {
            return null;
        }
        try {
            return DateTimeFormatter.ISO_INSTANT.parse(dueAt, Instant::from);
        } catch (Exception e) {
            throw new BizException(ErrorCode.PARAM_INVALID, "到期时间格式无效");
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
