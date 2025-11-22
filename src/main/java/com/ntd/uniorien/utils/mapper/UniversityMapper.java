package com.ntd.uniorien.utils.mapper;

import com.ntd.uniorien.dto.request.UniversityDetailCreationRequest;
import com.ntd.uniorien.dto.response.UniversityDetailResponse;
import com.ntd.uniorien.dto.response.UniversityReviewResponse;
import com.ntd.uniorien.entity.University;
import com.ntd.uniorien.entity.UniversityInformation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UniversityMapper {
    UniversityReviewResponse toUniversityResponse(University university);

    @Mapping(source = "universityInformation.name", target = "universityName")
    @Mapping(source = "universityInformation.university.universityCode", target = "universityCode")
    @Mapping(source = "universityInformation.university.id", target = "universityId")
    UniversityDetailResponse toUniversityDetailResponse(UniversityInformation universityInformation);

    UniversityInformation toUniversityInformation(UniversityDetailCreationRequest universityDetailCreationRequest);
}
