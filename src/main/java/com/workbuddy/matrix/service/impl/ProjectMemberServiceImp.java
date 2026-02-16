package com.workbuddy.matrix.service.impl;

import com.workbuddy.matrix.dto.member.InviteMemberRequest;
import com.workbuddy.matrix.dto.member.MemberResponse;
import com.workbuddy.matrix.entity.Project;
import com.workbuddy.matrix.entity.ProjectMemberId;
import com.workbuddy.matrix.entity.ProjectMembers;
import com.workbuddy.matrix.entity.User;
import com.workbuddy.matrix.error.ResourceNotFoundException;
import com.workbuddy.matrix.mapper.ProjectMembersMapper;
import com.workbuddy.matrix.repository.ProjectMembersRepository;
import com.workbuddy.matrix.repository.ProjectRepository;
import com.workbuddy.matrix.repository.UserRepository;
import com.workbuddy.matrix.security.AuthUtil;
import com.workbuddy.matrix.service.ProjectMemberService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.List;

@Service
@FieldDefaults(makeFinal = true,level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class ProjectMemberServiceImp implements ProjectMemberService {

    ProjectMembersRepository projectMembersRepository;
    ProjectRepository projectRepository;
    UserRepository userRepository;
    ProjectMembersMapper projectMembersMapper;
    AuthUtil authUtil;

    @Override
    @PreAuthorize("@security.canViewMembers(#projectId)")
    public List<MemberResponse> getProjectMembers(Long projectId) {
        return projectMembersRepository.findByIdProjectId(projectId)
                .stream()
                .map(projectMembersMapper::toProjectMemberResponseFromMember)
                .toList();
    }

    @Override
    @PreAuthorize("@security.canManageMembers(#projectId)")
    public MemberResponse inviteMember(Long projectId, InviteMemberRequest inviteMemberRequest) {
        Long userId = authUtil.getCurrentUserId();
        Project project = getAccessibleProjectById(projectId,userId);
        User invitee = userRepository.findByEmail(inviteMemberRequest.email())
                .orElseThrow(() -> new ResourceNotFoundException("invitee",inviteMemberRequest.email()));

        if(invitee.getId().equals(userId)){
            throw new RuntimeException("Cannot invite yourself");
        }

        ProjectMemberId projectMemberId = new ProjectMemberId(projectId,invitee.getId());

        if(projectMembersRepository.existsById(projectMemberId)){
            throw new RuntimeException("Cannot invite once again");
        }
        ProjectMembers members = ProjectMembers.builder()
                .id(projectMemberId)
                .project(project)
                .user(invitee)
                .role(inviteMemberRequest.role())
                .invitedAt(Instant.now())
                .build();
        projectMembersRepository.save(members);
        return projectMembersMapper.toProjectMemberResponseFromMember(members);
    }

    @Override
    @PreAuthorize("@security.canManageMembers(#projectId)")
    public MemberResponse updateMemberRole(Long projectId, Long memberId, InviteMemberRequest request) {
        ProjectMemberId projectMemberId = new ProjectMemberId(projectId,memberId);

        ProjectMembers projectMembers = projectMembersRepository.findById(projectMemberId).orElseThrow();

        projectMembers.setRole(request.role());
       projectMembers =  projectMembersRepository.save(projectMembers);

        return projectMembersMapper.toProjectMemberResponseFromMember(projectMembers);
    }

    @Override
    @PreAuthorize("@security.canManageMembers(#projectId)")
    public void deleteProjectMember(Long projectId, Long memberId) {
        ProjectMemberId projectMemberId = new ProjectMemberId(projectId,memberId);

        if(!projectMembersRepository.existsById(projectMemberId)){
            throw new RuntimeException("member not found in project");
        }

        projectMembersRepository.deleteById(projectMemberId);
    }

    // internal function
    private Project getAccessibleProjectById(Long projectId , Long userId){
        return projectRepository.findAccessibleByProjectId(projectId,userId)
                .orElseThrow(() -> new ResourceNotFoundException("project",projectId.toString()));
    }
}
