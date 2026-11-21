package com.workbuddy.matrixai.workspace_service.repository;

import com.workbuddy.matrixai.workspace_service.entity.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, String> {
}
