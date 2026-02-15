package com.workbuddy.matrix.service;

import com.workbuddy.matrix.dto.member.InviteMemberRequest;
import com.workbuddy.matrix.dto.member.MemberResponse;
import com.workbuddy.matrix.entity.ProjectMembers;

import java.util.List;

public interface ProjectMemberService {
    public List<MemberResponse> getProjectMembers(Long projectId);

    public MemberResponse inviteMember(Long projectId, InviteMemberRequest inviteMemberRequest);

    MemberResponse updateMemberRole(Long projectId, Long memberId, InviteMemberRequest request);

    void deleteProjectMember(Long projectId, Long memberId);
}
