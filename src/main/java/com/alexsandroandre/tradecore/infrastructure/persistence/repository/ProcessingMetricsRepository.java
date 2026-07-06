package com.alexsandroandre.tradecore.infrastructure.persistence.repository;

import com.alexsandroandre.tradecore.infrastructure.persistence.entity.ProcessingMetricsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

@Repository
public interface ProcessingMetricsRepository extends JpaRepository<ProcessingMetricsEntity, UUID> {

    @Query("SELECT m FROM ProcessingMetricsEntity m WHERE m.startTime BETWEEN :startTime AND :endTime ORDER BY m.startTime DESC")
    List<ProcessingMetricsEntity> findByDateRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
}