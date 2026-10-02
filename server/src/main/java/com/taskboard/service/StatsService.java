package com.taskboard.service;

import com.taskboard.dto.StatsByProjectItem;
import com.taskboard.dto.StatsByStatusItem;
import com.taskboard.dto.StatsByWeekItem;
import com.taskboard.entity.Project;
import com.taskboard.entity.Task;
import com.taskboard.entity.TimeLog;
import com.taskboard.repository.ProjectRepository;
import com.taskboard.repository.TaskRepository;
import com.taskboard.repository.TimeLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for statistical aggregations across tasks and time logs.
 */
@Service
public class StatsService {

    private final TaskRepository taskRepository;
    private final TimeLogRepository timeLogRepository;
    private final ProjectRepository projectRepository;

    public StatsService(TaskRepository taskRepository, TimeLogRepository timeLogRepository, ProjectRepository projectRepository) {
        this.taskRepository = taskRepository;
        this.timeLogRepository = timeLogRepository;
        this.projectRepository = projectRepository;
    }

    /**
     * Aggregate tasks and hours by project.
     */
    @Transactional(readOnly = true)
    public List<StatsByProjectItem> getByProject() {
        List<Task> allTasks = taskRepository.findAllByOrderByCreatedAtDesc();
        List<TimeLog> allLogs = timeLogRepository.findAllOrdered();

        // Build project name lookup
        Map<Long, String> projectNameMap = projectRepository.findAll().stream()
            .collect(Collectors.toMap(Project::getId, Project::getName));

        // Group logs by taskId
        Map<Long, Long> hoursByTask = allLogs.stream()
            .collect(Collectors.groupingBy(
                log -> log.getTask().getId(),
                Collectors.summingLong(log -> parseHours(log.getHours()))
            ));

        // Group by projectId
        Map<Long, List<Task>> tasksByProject = allTasks.stream()
            .collect(Collectors.groupingBy(Task::getProjectId));

        return tasksByProject.entrySet().stream()
            .map(entry -> {
                Long projectId = entry.getKey();
                List<Task> tasks = entry.getValue();
                long taskCount = tasks.size();
                long totalHours = tasks.stream()
                    .mapToLong(t -> hoursByTask.getOrDefault(t.getId(), 0L))
                    .sum();

                // Get project name
                String projectName = projectNameMap.getOrDefault(projectId, "项目 #" + projectId);

                return new StatsByProjectItem(projectId, projectName, taskCount, totalHours);
            })
            .sorted(Comparator.comparing(StatsByProjectItem::projectId))
            .toList();
    }

    /**
     * Aggregate hours by week based on time log work dates.
     */
    @Transactional(readOnly = true)
    public List<StatsByWeekItem> getByWeek(LocalDate weekStart, LocalDate weekEnd) {
        List<TimeLog> logs;
        if (weekStart != null || weekEnd != null) {
            logs = timeLogRepository.findByDateRange(weekStart, weekEnd);
        } else {
            logs = timeLogRepository.findAllOrdered();
        }

        WeekFields weekFields = WeekFields.ISO;

        // Group by ISO week
        Map<String, List<TimeLog>> byWeek = logs.stream()
            .collect(Collectors.groupingBy(log -> {
                LocalDate date = log.getWorkDate();
                int year = date.getYear();
                int week = date.get(weekFields.weekOfWeekBasedYear());
                return String.format("%d-W%02d", year, week);
            }));

        return byWeek.entrySet().stream()
            .map(entry -> {
                String week = entry.getKey();
                List<TimeLog> weekLogs = entry.getValue();
                long totalHours = weekLogs.stream()
                    .mapToLong(log -> parseHours(log.getHours()))
                    .sum();
                long taskCount = weekLogs.stream()
                    .map(log -> log.getTask().getId())
                    .distinct()
                    .count();
                return new StatsByWeekItem(week, totalHours, taskCount);
            })
            .sorted(Comparator.comparing(StatsByWeekItem::week))
            .toList();
    }

    /**
     * Aggregate task count and hours by status.
     */
    @Transactional(readOnly = true)
    public List<StatsByStatusItem> getByStatus() {
        List<Task> allTasks = taskRepository.findAllByOrderByCreatedAtDesc();
        List<TimeLog> allLogs = timeLogRepository.findAllOrdered();

        // Group logs by taskId
        Map<Long, Long> hoursByTask = allLogs.stream()
            .collect(Collectors.groupingBy(
                log -> log.getTask().getId(),
                Collectors.summingLong(log -> parseHours(log.getHours()))
            ));

        // Group tasks by status
        Map<String, List<Task>> byStatus = allTasks.stream()
            .collect(Collectors.groupingBy(Task::getStatus));

        // Ensure all four statuses are present
        List<String> allStatuses = List.of("TODO", "IN_PROGRESS", "DONE", "CLOSED");
        return allStatuses.stream()
            .map(status -> {
                List<Task> tasks = byStatus.getOrDefault(status, List.of());
                long count = tasks.size();
                long totalHours = tasks.stream()
                    .mapToLong(t -> hoursByTask.getOrDefault(t.getId(), 0L))
                    .sum();
                return new StatsByStatusItem(status, count, totalHours);
            })
            .toList();
    }

    private long parseHours(String hours) {
        if (hours == null || hours.isBlank()) return 0;
        try {
            return Long.parseLong(hours.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
