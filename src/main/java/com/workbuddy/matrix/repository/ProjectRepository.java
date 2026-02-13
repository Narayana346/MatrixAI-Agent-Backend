package com.workbuddy.matrix.repository;

import com.workbuddy.matrix.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    @Query("""
            SELECT p FROM Project p
            WHERE p.owner.id = :ownerId
            AND p.deletedAt IS NULL
            ORDER BY p.updatedAt DESC
            """)
    List<Project> findAllAccessibleByUserId(@Param("ownerId") Long ownerId);

    Optional<Project> findByIdAndOwnerId(Long id, Long ownerId);
}
