package com.workbuddy.matrix.repository;

import com.workbuddy.matrix.entity.ProjectMemberId;
import com.workbuddy.matrix.entity.ProjectMembers;
import com.workbuddy.matrix.enums.ProjectRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectMembersRepository extends JpaRepository<ProjectMembers, ProjectMemberId> {
    List<ProjectMembers> findByIdProjectId(Long projectId);

    @Query("""
            SELECT pm.role FROM ProjectMembers pm
            WHERE pm.id.projectId = :projectId
            AND pm.id.userId = :userId
           """)
    Optional<ProjectRole> findRoleByProjectIdAndUserId(@Param("projectId") Long projectId, @Param("userId") Long userId);
}
