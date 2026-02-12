package com.workbuddy.matrix.repository;

import com.workbuddy.matrix.entity.ProjectFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectFileRepository extends JpaRepository<ProjectFile,Long> {
    List<ProjectFile> findByProjectId(Long projectId);
}
