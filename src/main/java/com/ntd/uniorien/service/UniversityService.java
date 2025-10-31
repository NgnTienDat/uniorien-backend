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

import java.time.LocalDate;
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

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUniversities() {
        universityRepository.deleteAll();
    }

    public UniversityResponse getAdmissionsByUniversityCode(
            String universityCode,
            String year,
            String admissionMethod
    ) {
        int yearInt;
        if (year == null || year.isEmpty()) {
            yearInt = LocalDate.now().getYear();
        } else {
            try {
                yearInt = Integer.parseInt(year);
            } catch (NumberFormatException e) {
                throw new AppException(ErrorCode.INVALID_YEAR_FORMAT);
            }
        }

        University university = universityRepository
                .findWithBenchmarks(universityCode.toUpperCase())
                .orElseThrow(() -> new AppException(ErrorCode.UNIVERSITY_NOT_FOUND));

        List<AdmissionResponse> admissions = university.getAdmissionInformations().stream()
                .filter(ai -> ai.getYearOfAdmission() == yearInt)
                .filter(ai -> admissionMethod == null || admissionMethod.isEmpty()
                        || ai.getAdmissionMethod().equalsIgnoreCase(admissionMethod))
                .map(ai -> {
                    List<BenchmarkResponse> benchmarks = ai.getBenchmarks().stream()
                            .map(b -> BenchmarkResponse.builder()
                                    .majorCode(b.getMajor().getMajorCode())
                                    .major(b.getMajor().getMajorName())
                                    .score(b.getScore())
                                    .subjectCombinations(b.getSubjectCombinations())
                                    .note(b.getNote())
                                    .build())
                            .toList();

                    return AdmissionResponse.builder()
                            .admissionMethod(ai.getAdmissionMethod())
                            .admissionYear(String.valueOf(ai.getYearOfAdmission()))
                            .benchmarkList(benchmarks)
                            .build();
                })
                .toList();

        return UniversityResponse.builder()
                .universityCode(university.getUniversityCode())
                .universityName(university.getUniversityName())
                .website(university.getWebsite())
                .admissionList(admissions)
                .build();
    }


}
