package com.taskboard.controller;

import com.taskboard.dto.ApiResponse;
import com.taskboard.dto.StatsByProjectItem;
import com.taskboard.dto.StatsByStatusItem;
import com.taskboard.dto.StatsByWeekItem;
import com.taskboard.service.StatsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for statistical reports.
 */
@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    /**
     * Get statistics aggregated by project.
     */
    @GetMapping("/by-assignee")
    public ApiResponse<List<StatsByProjectItem>> getByProject() {
        return ApiResponse.success(statsService.getByProject());
    }

    /**
     * Get statistics aggregated by week.
     */
    @GetMapping("/by-week")
    public ApiResponse<List<StatsByWeekItem>> getByWeek(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekEnd
    ) {
        return ApiResponse.success(statsService.getByWeek(weekStart, weekEnd));
    }

    /**
     * Get statistics aggregated by task status.
     */
    @GetMapping("/by-status")
    public ApiResponse<List<StatsByStatusItem>> getByStatus() {
        return ApiResponse.success(statsService.getByStatus());
    }
}
