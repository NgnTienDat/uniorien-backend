package com.ntd.uniorien.service;

import com.ntd.uniorien.dto.response.MajorDetailResponse;
import com.ntd.uniorien.dto.response.MajorFilterResponse;
import com.ntd.uniorien.dto.response.ScoreByYearResponse;
import com.ntd.uniorien.entity.*;
import com.ntd.uniorien.repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class BenchmarkService {

    UniversityRepository universityRepository;
    AdmissionInfoRepository admissionInfoRepository;
    BenchmarkRepository benchmarkRepository;
    MajorRepository majorRepository;

    public List<MajorFilterResponse> getMajorsByMajorGroup(String majorSearch,
                                                           String admissionSearch,
                                                           String location) {
        if (location == null || location.isEmpty()) {
            location = "";
        }
        List<Benchmark> benchmarks = benchmarkRepository
                .searchBenchmarks(majorSearch, admissionSearch, location);

        if (benchmarks.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, List<Benchmark>> groupedByUniversity = benchmarks.stream()
                .collect(Collectors.groupingBy(b -> b.getUniversity().getUniversityName()));

        List<MajorFilterResponse> responseList = new ArrayList<>();

        for (Map.Entry<String, List<Benchmark>> entry : groupedByUniversity.entrySet()) {
            String universityName = entry.getKey();
            List<Benchmark> universityBenchmarks = entry.getValue();

            Map<String, List<Benchmark>> groupedByMajor = universityBenchmarks.stream()
                    .collect(Collectors.groupingBy(b -> b.getMajor().getMajorName()));

            List<MajorDetailResponse> majorDetailResponses = new ArrayList<>();

            for (Map.Entry<String, List<Benchmark>> majorEntry : groupedByMajor.entrySet()) {
                String majorName = majorEntry.getKey();
                List<Benchmark> majorBenchmarks = majorEntry.getValue();

                majorBenchmarks.sort(Comparator.comparingInt(
                        b -> -b.getAdmissionInformation().getYearOfAdmission()
                ));

                List<ScoreByYearResponse> scores = majorBenchmarks.stream()
                        .sorted(Comparator.comparingInt(b -> -b.getAdmissionInformation().getYearOfAdmission()))
                        .map(b -> new ScoreByYearResponse(
                                b.getAdmissionInformation().getYearOfAdmission(),
                                b.getScore(),
                                b.getSubjectCombinations()
                        ))
                        .toList();

                String subjectCombinations = majorBenchmarks.get(0).getSubjectCombinations();

                majorDetailResponses.add(MajorDetailResponse.builder()
                        .majorName(majorName)
                        .subjectCombinations(subjectCombinations)
                        .scores(scores)
                        .build());
            }

            responseList.add(MajorFilterResponse.builder()
                    .universityName(universityName)
                    .majorDetails(majorDetailResponses)
                    .build());
        }

        return responseList;
    }


    public void handleSaveBenchmarkFromFileCSV(MultipartFile file) {

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(),
                        StandardCharsets.UTF_8))) {

            String header = reader.readLine();
            if (header == null) {
                log.warn("Empty CSV file!");
                return;
            }

            Map<String, University> universityCache = universityRepository.findAll().stream()
                    .collect(Collectors.toMap(University::getUniversityCode, u -> u));

            Map<String, Major> majorCache = majorRepository.findAll().stream()
                    .collect(Collectors.toMap(Major::getMajorCode, m -> m));

            Map<String, AdmissionInformation> admissionCache = new HashMap<>();

            System.out.println(universityCache);

            String line;
            int count = 0;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] cols = line.split(",", -1);
                if (cols.length < 9) {
                    log.warn("Invalid row skipped: {}", line);
                    continue;
                }

                // Parse dữ liệu
                String universityCode = cols[0].trim();
                String universityName = cols[1].trim();
                String website = cols[2].trim();
                String yearStr = cols[3].trim();
                String admissionMethod = cols[4].trim();
                String majorCode = cols[5].trim();
                String majorName = cols[6].trim();
                String subjectCombinations = cols[7].trim();
                String scoreStr = cols[8].trim();
                String note = cols.length > 9 ? cols[9].trim() : null;

                University university = universityCache.get(universityCode);
                if (university == null) {
                    log.warn("University not found (skipped): {}", universityCode);
                    continue;
                }

                int yearInt = 0;
                try {
                    yearInt = Integer.parseInt(yearStr);
                } catch (NumberFormatException e) {
                    log.warn("Invalid year: {}", yearStr);
                    continue;
                }

                String admissionKey = university.getId() + "_" + yearInt + "_" + admissionMethod;
                AdmissionInformation admissionInformation = admissionCache.get(admissionKey);

                if (admissionInformation == null) {
                    admissionInformation = new AdmissionInformation();
                    admissionInformation.setYearOfAdmission(yearInt);
                    admissionInformation.setAdmissionMethod(admissionMethod);
                    admissionInformation.setUniversity(university);
                    admissionInformation = admissionInfoRepository.save(admissionInformation);
                    admissionCache.put(admissionKey, admissionInformation);
                }

                Major major = majorCache.get(majorCode);
                if (major == null) {
                    major = new Major();
                    major.setMajorCode(majorCode);
                    major.setMajorName(majorName);
                    major = majorRepository.save(major);
                    majorCache.put(majorCode, major);
                }

                float score = 0;
                try {
                    if (!scoreStr.isEmpty()) score = Float.parseFloat(scoreStr);
                } catch (NumberFormatException e) {
                    log.warn("Invalid score: {}", scoreStr);
                }

                Benchmark benchmark = Benchmark.builder()
                        .score(score)
                        .note(note)
                        .subjectCombinations(subjectCombinations)
                        .major(major)
                        .university(university)
                        .admissionInformation(admissionInformation)
                        .build();

                benchmarkRepository.save(benchmark);
                count++;
            }

            log.info("Import completed: {} benchmarks saved.", count);

        } catch (Exception e) {
            log.error("Error while reading CSV file: ", e);
        }
    }
}
