package com.workbuddy.matrix.service;

import com.workbuddy.matrix.dto.member.InviteMemberRequest;
import com.workbuddy.matrix.dto.member.MemberResponse;
import com.workbuddy.matrix.entity.ProjectMember;

import java.util.List;

public interface ProjectMemberService {
    public List<ProjectMember> getProjectMembers(Long projectId, Long userId);

    public MemberResponse inviteMember(Long projectId, InviteMemberRequest inviteMemberRequest, Long userId);

    ProjectMemberService updateMemberRole(Long projectId, Long memberId, Long userId, InviteMemberRequest request);

    MemberResponse deleteProjectMember(Long projectId, Long memberId, Long userId);
}
