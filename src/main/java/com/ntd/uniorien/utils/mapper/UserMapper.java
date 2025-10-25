package com.ntd.uniorien.utils.mapper;

import com.ntd.uniorien.dto.request.UserCreationRequest;
import com.ntd.uniorien.dto.response.UserResponse;
import com.ntd.uniorien.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreationRequest request);
    UserResponse toUserResponse(User user);
}
