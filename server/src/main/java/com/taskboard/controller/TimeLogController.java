package com.taskboard.controller;

import com.taskboard.common.PageResult;
import com.taskboard.dto.ApiResponse;
import com.taskboard.dto.CreateTimeLogRequest;
import com.taskboard.dto.TimeLogDto;
import com.taskboard.service.TimeLogService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for TimeLog management.
 */
@RestController
@RequestMapping("/api/timelogs")
public class TimeLogController {

    private final TimeLogService timeLogService;

    public TimeLogController(TimeLogService timeLogService) {
        this.timeLogService = timeLogService;
    }

    /**
     * Get time logs with pagination, optionally filtered by taskId.
     */
    @GetMapping
    public ApiResponse<PageResult<TimeLogDto>> getTimeLogs(
            @RequestParam(required = false) Long taskId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        if (size > 100) {
            size = 100;
        }
        Page<TimeLogDto> result = timeLogService.findAll(taskId, page, size);
        PageResult<TimeLogDto> pageResult = PageResult.of(
            result.getContent(),
            result.getTotalElements(),
            page,
            size
        );
        return ApiResponse.success(pageResult);
    }

    /**
     * Create a new time log entry.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TimeLogDto> createTimeLog(CreateTimeLogRequest request) {
        TimeLogDto created = timeLogService.create(request);
        return ApiResponse.success(created);
    }

    /**
     * Delete a time log entry.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTimeLog(@PathVariable Long id) {
        timeLogService.delete(id);
    }
}
