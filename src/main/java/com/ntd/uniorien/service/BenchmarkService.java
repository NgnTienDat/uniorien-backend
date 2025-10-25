package com.ntd.uniorien.service;

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
import java.io.FileReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
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


    public void handleSaveBenchmarkFromFileCSV(MultipartFile file) {

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(),
                        StandardCharsets.UTF_8))) {


            // Bỏ dòng header
            String header = reader.readLine();
            if (header == null) {
                log.warn("Empty CSV file!");
                return;
            }

            // Dùng cache để giảm truy vấn DB lặp lại
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

                String[] cols = line.split(",", -1); // Giữ cả cột rỗng
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

                // Check University
                University university = universityCache.get(universityCode);
                if (university == null) {
                    log.warn("University not found (skipped): {}", universityCode);
                    continue; // bỏ qua trường không tồn tại
                }

                // Parse năm
                int yearInt = 0;
                try {
                    yearInt = Integer.parseInt(yearStr);
                } catch (NumberFormatException e) {
                    log.warn("Invalid year: {}", yearStr);
                    continue;
                }

                // Tạo hoặc lấy AdmissionInformation
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

                // Major
                Major major = majorCache.get(majorCode);
                if (major == null) {
                    major = new Major();
                    major.setMajorCode(majorCode);
                    major.setMajorName(majorName);
                    major = majorRepository.save(major);
                    majorCache.put(majorCode, major);
                }

                // Parse điểm
                float score = 0;
                try {
                    if (!scoreStr.isEmpty()) score = Float.parseFloat(scoreStr);
                } catch (NumberFormatException e) {
                    log.warn("Invalid score: {}", scoreStr);
                }

                // Tạo Benchmark
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
