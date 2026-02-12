package com.workbuddy.matrix.service.impl;

import com.workbuddy.matrix.dto.member.InviteMemberRequest;
import com.workbuddy.matrix.dto.member.MemberResponse;
import com.workbuddy.matrix.entity.ProjectMember;
import com.workbuddy.matrix.service.ProjectMemberService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectMemberServiceImp implements ProjectMemberService {

    @Override
    public List<ProjectMember> getProjectMembers(Long projectId, Long userId) {
        return List.of();
    }

    @Override
    public MemberResponse inviteMember(Long projectId, InviteMemberRequest inviteMemberRequest, Long userId) {
        return null;
    }

    @Override
    public ProjectMemberService updateMemberRole(Long projectId, Long memberId, Long userId, InviteMemberRequest request) {
        return null;
    }

    @Override
    public MemberResponse deleteProjectMember(Long projectId, Long memberId, Long userId) {
        return null;
    }
}
