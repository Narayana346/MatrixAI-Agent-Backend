package com.workbuddy.matrix.mapper;

import com.workbuddy.matrix.dto.member.MemberResponse;
import com.workbuddy.matrix.entity.ProjectMembers;
import com.workbuddy.matrix.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProjectMembersMapper {
    @Mapping(target = "userId", source = "id")
    @Mapping(target = "role",constant = "OWNER")
    MemberResponse toProjectMemberResponseFromOwner(User owner);

    @Mapping(target = "userId",source = "user.id")
    @Mapping(target = "email",source = "user.email")
    @Mapping(target = "name",source = "user.name")
    @Mapping(target = "avatarUrl",source = "user.avatarUrl")
    MemberResponse toProjectMemberResponseFromMember(ProjectMembers projectMembers);


}
