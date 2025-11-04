package com.ntd.uniorien.service;

import com.ntd.uniorien.dto.response.UniversityReviewResponse;
import com.ntd.uniorien.entity.University;
import com.ntd.uniorien.repository.UniversityRepository;
import com.ntd.uniorien.utils.mapper.UniversityMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ReviewService {
    UniversityRepository universityRepository;
    UniversityMapper universityMapper;


    public List<UniversityReviewResponse> allUniversityReviews() {
        List<University> universities = universityRepository.findAll();
        return universities.stream().map(universityMapper::toUniversityResponse).toList();
    }



}
