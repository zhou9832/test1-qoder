package com.taskboard.service;

import com.taskboard.common.BizException;
import com.taskboard.common.ErrorCode;
import com.taskboard.dto.CreateTimeLogRequest;
import com.taskboard.dto.TimeLogDto;
import com.taskboard.entity.Task;
import com.taskboard.entity.TimeLog;
import com.taskboard.repository.TaskRepository;
import com.taskboard.repository.TimeLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Business logic for TimeLog management.
 */
@Service
public class TimeLogService {

    private final TimeLogRepository timeLogRepository;
    private final TaskRepository taskRepository;

    public TimeLogService(TimeLogRepository timeLogRepository, TaskRepository taskRepository) {
        this.timeLogRepository = timeLogRepository;
        this.taskRepository = taskRepository;
    }

    /**
     * Get time logs with pagination, optionally filtered by taskId.
     */
    @Transactional(readOnly = true)
    public Page<TimeLogDto> findAll(Long taskId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page - 1, size);
        Page<TimeLog> logs;
        if (taskId != null) {
            logs = timeLogRepository.findByTaskIdOrderByWorkDateDesc(taskId, pageRequest);
        } else {
            logs = timeLogRepository.findAllByOrderByWorkDateDesc(pageRequest);
        }
        return logs.map(TimeLogDto::from);
    }

    /**
     * Create a new time log entry.
     */
    @Transactional
    public TimeLogDto create(CreateTimeLogRequest request) {
        // Validate required fields
        if (request.taskId() == null) {
            throw new BizException(ErrorCode.PARAM_REQUIRED, "任务ID不能为空");
        }
        if (request.hours() == null || request.hours().isBlank()) {
            throw new BizException(ErrorCode.PARAM_REQUIRED, "工时不能为空");
        }
        if (request.workDate() == null || request.workDate().isBlank()) {
            throw new BizException(ErrorCode.PARAM_REQUIRED, "工作日期不能为空");
        }

        // Validate hours is a positive integer
        int hoursValue;
        try {
            hoursValue = Integer.parseInt(request.hours().trim());
            if (hoursValue <= 0) {
                throw new BizException(ErrorCode.PARAM_INVALID, "工时必须为正整数");
            }
        } catch (NumberFormatException e) {
            throw new BizException(ErrorCode.PARAM_INVALID, "工时必须为整数");
        }

        // Validate workDate format
        LocalDate workDate;
        try {
            workDate = LocalDate.parse(request.workDate().trim());
        } catch (DateTimeParseException e) {
            throw new BizException(ErrorCode.PARAM_INVALID, "工作日期格式无效，应为 YYYY-MM-DD");
        }

        // Validate note length
        if (request.note() != null && request.note().length() > 256) {
            throw new BizException(ErrorCode.PARAM_LENGTH_EXCEEDED, "备注不能超过256个字符");
        }

        // Validate task exists
        Task task = taskRepository.findById(request.taskId())
            .orElseThrow(() -> new BizException(ErrorCode.PARAM_NOT_FOUND, "任务不存在"));

        TimeLog log = new TimeLog();
        log.setTask(task);
        log.setHours(String.valueOf(hoursValue));
        log.setWorkDate(workDate);
        log.setNote(request.note() != null ? request.note().trim() : null);

        TimeLog saved = timeLogRepository.save(log);
        return TimeLogDto.from(saved);
    }

    /**
     * Delete a time log entry.
     */
    @Transactional
    public void delete(Long id) {
        if (!timeLogRepository.existsById(id)) {
            throw new BizException(ErrorCode.PARAM_NOT_FOUND, "工时记录不存在");
        }
        timeLogRepository.deleteById(id);
    }
}
