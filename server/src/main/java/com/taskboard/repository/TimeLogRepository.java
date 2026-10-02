package com.taskboard.repository;

import com.taskboard.entity.TimeLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TimeLogRepository extends JpaRepository<TimeLog, Long> {

    Page<TimeLog> findAllByOrderByWorkDateDesc(Pageable pageable);

    Page<TimeLog> findByTaskIdOrderByWorkDateDesc(Long taskId, Pageable pageable);

    @Query("SELECT t FROM TimeLog t WHERE " +
           "(:weekStart IS NULL OR t.workDate >= :weekStart) AND " +
           "(:weekEnd IS NULL OR t.workDate <= :weekEnd) " +
           "ORDER BY t.workDate DESC")
    List<TimeLog> findByDateRange(@Param("weekStart") LocalDate weekStart,
                                  @Param("weekEnd") LocalDate weekEnd);

    @Query("SELECT t FROM TimeLog t ORDER BY t.workDate DESC")
    List<TimeLog> findAllOrdered();
}
