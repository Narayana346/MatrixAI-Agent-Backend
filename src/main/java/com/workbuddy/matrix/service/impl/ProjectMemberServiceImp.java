package com.workbuddy.matrix.service.impl;

import com.workbuddy.matrix.dto.member.InviteMemberRequest;
import com.workbuddy.matrix.dto.member.MemberResponse;
import com.workbuddy.matrix.entity.Project;
import com.workbuddy.matrix.entity.ProjectMemberId;
import com.workbuddy.matrix.entity.ProjectMembers;
import com.workbuddy.matrix.entity.User;
import com.workbuddy.matrix.mapper.ProjectMembersMapper;
import com.workbuddy.matrix.repository.ProjectMembersRepository;
import com.workbuddy.matrix.repository.ProjectRepository;
import com.workbuddy.matrix.repository.UserRepository;
import com.workbuddy.matrix.service.ProjectMemberService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@FieldDefaults(makeFinal = true,level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public class ProjectMemberServiceImp implements ProjectMemberService {

    ProjectMembersRepository projectMembersRepository;
    ProjectRepository projectRepository;
    UserRepository userRepository;
    ProjectMembersMapper projectMembersMapper;

    @Override
    public List<MemberResponse> getProjectMembers(Long projectId, Long userId) {
        Project project = getAccessibleProjectById(projectId,userId);

        List<MemberResponse> memberResponseList = new ArrayList<>();
        memberResponseList.add(projectMembersMapper.toProjectMemberResponseFromOwner(project.getOwner()));

        memberResponseList.addAll(
                projectMembersRepository.findByIdProjectId(projectId)
                .stream()
                .map(projectMembersMapper::toProjectMemberResponseFromMember)
                .toList()
        );

        return memberResponseList;
    }

    @Override
    public MemberResponse inviteMember(Long projectId, InviteMemberRequest inviteMemberRequest, Long userId) {
        Project project = getAccessibleProjectById(projectId,userId);
        if(!project.getOwner().getId().equals(userId)){
            throw new RuntimeException("Not Allowed");
        }

        User invitee = userRepository.findByEmail(inviteMemberRequest.email()).orElseThrow();

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
    public MemberResponse updateMemberRole(Long projectId, Long memberId, Long userId, InviteMemberRequest request) {
        Project project = getAccessibleProjectById(projectId,userId);

        if (!project.getOwner().getId().equals(userId)){
            throw new RuntimeException("not Allowed");
        }

        ProjectMemberId projectMemberId = new ProjectMemberId(projectId,memberId);

        ProjectMembers projectMembers = projectMembersRepository.findById(projectMemberId).orElseThrow();

        projectMembers.setRole(request.role());
       projectMembers =  projectMembersRepository.save(projectMembers);

        return projectMembersMapper.toProjectMemberResponseFromMember(projectMembers);
    }

    @Override
    public void deleteProjectMember(Long projectId, Long memberId, Long userId) {
        Project project = getAccessibleProjectById(projectId,userId);

        if (!project.getOwner().getId().equals(userId)){
            throw new RuntimeException("not Allowed");
        }

        ProjectMemberId projectMemberId = new ProjectMemberId(projectId,memberId);

        if(!projectMembersRepository.existsById(projectMemberId)){
            throw new RuntimeException("member not found in project");
        }

        projectMembersRepository.deleteById(projectMemberId);
    }

    // internal function
    private Project getAccessibleProjectById(Long projectId , Long userId){
        return projectRepository.findAccessibleByProjectId(projectId,userId).orElseThrow();
    }
}
