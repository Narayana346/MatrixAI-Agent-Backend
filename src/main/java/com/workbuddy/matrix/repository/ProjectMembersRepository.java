package com.workbuddy.matrix.repository;

import com.workbuddy.matrix.entity.ProjectMemberId;
import com.workbuddy.matrix.entity.ProjectMembers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectMembersRepository extends JpaRepository<ProjectMembers, ProjectMemberId> {
    List<ProjectMembers> findByIdProjectId(Long projectId);
}
