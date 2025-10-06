package com.ntd.uniorien.utils.mapper;

import com.ntd.uniorien.dto.request.UserCreation;
import com.ntd.uniorien.dto.response.UserResponse;
import com.ntd.uniorien.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreation request);
    UserResponse toUserResponse(User user);
}
