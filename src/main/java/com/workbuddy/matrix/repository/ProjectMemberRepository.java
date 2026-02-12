package com.workbuddy.matrix.repository;

import com.workbuddy.matrix.entity.ProjectMember;
import com.workbuddy.matrix.entity.ProjectMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, ProjectMemberId> {}
