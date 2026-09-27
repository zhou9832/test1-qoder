package com.taskboard.controller;

import com.taskboard.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Health check endpoint for monitoring.
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, String>>> health() {
        Map<String, String> status = Map.of(
            "status", "UP",
            "database", "connected",
            "timestamp", java.time.Instant.now().toString()
        );
        return ResponseEntity.ok(ApiResponse.success(status));
    }
}
