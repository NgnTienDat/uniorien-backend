package com.ntd.uniorien.service;

import com.ntd.uniorien.dto.response.AdmissionResponse;
import com.ntd.uniorien.dto.response.BenchmarkResponse;
import com.ntd.uniorien.dto.response.UniversityResponse;
import com.ntd.uniorien.enums.ErrorCode;
import com.ntd.uniorien.exception.AppException;
import com.ntd.uniorien.utils.raw.SchoolInfo;
import com.ntd.uniorien.entity.University;
import com.ntd.uniorien.repository.UniversityRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UniversityService {
    UniversityRepository universityRepository;

    @PreAuthorize("hasRole('ADMIN')")
    public void saveUniversities(List<SchoolInfo> schools) {
        Set<String> existedCodes = new HashSet<>(universityRepository.findAllCodes());

        List<University> newUniversities = schools.stream()
                .filter(school -> !existedCodes.contains(school.getCode()))
                .map(school -> University.builder()
                        .universityCode(school.getCode())
                        .universityName(school.getName())
                        .website(school.getUrl())
                        .build())
                .toList();

        if (!newUniversities.isEmpty()) {
            universityRepository.saveAll(newUniversities);
        }
    }

    public List<UniversityResponse> getAllUniversities() {
        return universityRepository.findAllCodeAndName();
    }

    public void deleteUniversities () {
        universityRepository.deleteAll();
    }

    public UniversityResponse getAdmissionsByUniversityCode(String universityCode) {
        University university = universityRepository
                .findByUniversityCode(universityCode.toUpperCase())
                .orElseThrow(() -> new AppException(ErrorCode.UNIVERSITY_NOT_FOUND));

        UniversityResponse universityResponse = UniversityResponse.builder()
                .universityCode(university.getUniversityCode())
                .universityName(university.getUniversityName())
                .website(university.getWebsite())
                .admissionList(new ArrayList<>())
                .build();

        university.getAdmissionInformations().forEach(admissionInformation -> {

            AdmissionResponse admissionResponse = new AdmissionResponse();
            admissionResponse.setAdmissionMethod(admissionInformation.getAdmissionMethod());
            admissionResponse.setAdmissionYear(admissionInformation.getYearOfAdmission().toString());

            admissionInformation.getBenchmarks().forEach(benchmark -> {
                BenchmarkResponse benchmarkResponse = BenchmarkResponse.builder()
                        .majorCode(benchmark.getMajor().getMajorCode())
                        .score(benchmark.getScore())
                        .major(benchmark.getMajor().getMajorName())
                        .subjectCombinations(benchmark.getSubjectCombinations())
                        .note(benchmark.getNote())
                        .build();
                admissionResponse.getBenchmarkList().add(benchmarkResponse);
            });

            universityResponse.getAdmissionList().add(admissionResponse);
        });
        return universityResponse;
    }
}
