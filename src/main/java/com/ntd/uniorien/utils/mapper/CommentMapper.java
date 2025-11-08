package com.ntd.uniorien.utils.mapper;

import com.ntd.uniorien.dto.response.CommentResponse;
import com.ntd.uniorien.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(source = "user.fullName", target = "fullName")
    @Mapping(source = "user.avatar", target = "avatar")
    CommentResponse toCommentResponse(Comment comment);
}
