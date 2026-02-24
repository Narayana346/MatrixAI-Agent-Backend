package com.workbuddy.matrix.repository;

import com.workbuddy.matrix.entity.Project;
import com.workbuddy.matrix.enums.ProjectRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    @Query("""
            SELECT p as project, pm.role as role
            FROM Project p
            JOIN ProjectMembers pm ON pm.project.id = p.id
            WHERE pm.user.id = :userId
            AND p.deletedAt IS NULL
            ORDER BY p.updatedAt DESC
            """)
    List<ProjectWithRole> findAllAccessibleByUserId(@Param("userId") Long userId);

    @Query("""
            SELECT p FROM Project p
            WHERE p.id = :projectId
            AND p.deletedAt IS NULL
            AND EXISTS (
                SELECT 1 FROM ProjectMembers pm
                WHERE pm.project.id = :projectId
                AND pm.user.id = :userId
            )
            """)
    Optional<Project> findAccessibleByProjectId(@Param("projectId")Long projectId,
                                                @Param("userId")Long userId);

    @Query("""
            SELECT p as project, pm.role as role
            FROM Project p
            JOIN ProjectMembers pm ON pm.project.id = p.id
            WHERE p.id = :projectId
            AND p.deletedAt IS NULL
            AND pm.user.id = :userId
            """)
    Optional<ProjectWithRole> findAccessibleByProjectIdWithRole(@Param("projectId")Long projectId,
                                                @Param("userId")Long userId);



    interface ProjectWithRole{
        Project getProject();
        ProjectRole getRole();
    }

}
