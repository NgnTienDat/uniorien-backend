package com.ntd.uniorien.utils.mapper;

import com.ntd.uniorien.dto.response.RoleResponse;
import com.ntd.uniorien.entity.Role;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    RoleResponse toRoleResponse(Role role);
}
