package com.workbuddy.matrix.repository;

import com.workbuddy.matrix.entity.Project;
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
            SELECT p FROM Project p
            WHERE p.owner.id = :userId
            AND p.deletedAt IS NULL
            ORDER BY p.updatedAt DESC
            """)
    List<Project> findAllAccessibleByUserId(@Param("userId") Long userId);

    @Query("""
            SELECT p FROM Project p
            LEFT JOIN FETCH p.owner
            WHERE p.id = :projectId
            AND p.deletedAt IS NULL
            AND p.owner.id = :userId
            """)
    Optional<Project> findAccessibleByProjectId(@Param("projectId")Long projectId,
                                                @Param("userId")Long userId);

}
