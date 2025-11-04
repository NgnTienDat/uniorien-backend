package com.ntd.uniorien.utils.mapper;

import com.ntd.uniorien.dto.response.UniversityReviewResponse;
import com.ntd.uniorien.entity.University;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UniversityMapper {
    UniversityReviewResponse toUniversityResponse(University university);
}
