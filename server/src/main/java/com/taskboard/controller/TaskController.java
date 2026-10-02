package com.taskboard.controller;

import com.taskboard.dto.ApiResponse;
import com.taskboard.dto.CreateTaskRequest;
import com.taskboard.dto.TaskDto;
import com.taskboard.dto.UpdateTaskRequest;
import com.taskboard.service.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Task management with state machine transitions.
 */
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    /**
     * Get all tasks.
     */
    @GetMapping
    public ApiResponse<List<TaskDto>> getAllTasks() {
        List<TaskDto> tasks = taskService.findAll();
        return ApiResponse.success(tasks);
    }

    /**
     * Create a new task.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<TaskDto> createTask(CreateTaskRequest request) {
        TaskDto created = taskService.createTask(request);
        return ApiResponse.success(created);
    }

    /**
     * Get a task by ID.
     */
    @GetMapping("/{id}")
    public ApiResponse<TaskDto> getTaskById(@PathVariable Long id) {
        TaskDto task = taskService.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Task not found with id: " + id));
        return ApiResponse.success(task);
    }

    /**
     * Update an existing task.
     */
    @PutMapping("/{id}")
    public ApiResponse<TaskDto> updateTask(
            @PathVariable Long id,
            UpdateTaskRequest request
    ) {
        TaskDto updated = taskService.updateTask(id, request);
        return ApiResponse.success(updated);
    }

    /**
     * Transition task status using state machine rules.
     */
    @PatchMapping("/{id}/transitions")
    public ApiResponse<TaskDto> transitionStatus(
            @PathVariable Long id,
            @RequestParam String to
    ) {
        TaskDto updated = taskService.transitionStatus(id, to);
        return ApiResponse.success(updated);
    }

    /**
     * Delete a task by ID.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ApiResponse.success(null);
    }
}
