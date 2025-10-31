package com.ntd.uniorien.service;

import com.ntd.uniorien.dto.response.MajorGroupResponse;
import com.ntd.uniorien.entity.MajorForGroup;
import com.ntd.uniorien.enums.ErrorCode;
import com.ntd.uniorien.exception.AppException;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class MajorService {

    MajorRepository majorRepository;
    MajorForGroupRepository majorForGroupRepository;

    public void handleSaveMajorsAndMajorGroupsFromFileCSV(MultipartFile file) {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String header = reader.readLine(); // skip header line
            if (header == null) {
                log.warn("Empty CSV file!");
                return;
            }

            // Cache existing data to avoid duplicate inserts
            // Key format: "majorGroupName|majorName"
            Map<String, MajorForGroup> existingDataCache = majorForGroupRepository.findAll().stream()
                    .collect(Collectors.toMap(
                            mfg -> mfg.getMajorGroupName() + "|" + mfg.getMajorName(),
                            mfg -> mfg
                    ));

            List<MajorForGroup> toSave = new ArrayList<>();
            String line;
            int countNew = 0;

            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                // Split into max 3 columns
                String[] cols = line.split(",", 3);
                if (cols.length < 3) {
                    log.warn("Invalid row skipped: {}", line);
                    continue;
                }

                String groupName = cols[0].trim();
                String majorsCountStr = cols[1].trim();
                String majorsRaw = cols[2].trim();

                // Remove surrounding quotes if present
                if (majorsRaw.startsWith("\"") && majorsRaw.endsWith("\"")) {
                    majorsRaw = majorsRaw.substring(1, majorsRaw.length() - 1);
                }

                if (groupName.isEmpty() || majorsRaw.isEmpty()) {
                    log.warn("Missing group name or majors, skipped: {}", line);
                    continue;
                }

                // Split majors list
                String[] majors = majorsRaw.split(";");
                for (String mName : majors) {
                    String majorName = mName.trim();

                    // Remove any remaining quotes from individual major names
                    majorName = majorName.replaceAll("^\"|\"$", "");

                    if (majorName.isEmpty()) continue;

                    // Create unique key for cache lookup
                    String cacheKey = groupName + "|" + majorName;

                    // Check if this combination already exists
                    if (!existingDataCache.containsKey(cacheKey)) {
                        MajorForGroup majorForGroup = MajorForGroup.builder()
                                .majorName(majorName)
                                .majorGroupName(groupName)
                                .build();

                        toSave.add(majorForGroup);
                        existingDataCache.put(cacheKey, majorForGroup); // Add to cache to prevent duplicates in current batch
                        countNew++;
                    }
                }
            }

            // Batch save all new records
            if (!toSave.isEmpty()) {
                majorForGroupRepository.saveAll(toSave);
                log.info("Import completed: {} new major-group relationships saved.", countNew);
            } else {
                log.info("No new data to import. All records already exist.");
            }

        } catch (Exception e) {
            log.error("Error while reading CSV file: ", e);
            throw new RuntimeException("Failed to import CSV file", e);
        }
    }

    public List<MajorGroupResponse> getAllMajorGroups() {
        List<MajorForGroup> allMajorGroups = majorForGroupRepository.findAll();
        if (allMajorGroups.isEmpty()) {
            throw new AppException(ErrorCode.MAJOR_GROUP_EMPTY);
        }

        Map<String, List<String>> groupedMap = allMajorGroups.stream()
                .collect(Collectors.groupingBy(
                        MajorForGroup::getMajorGroupName,
                        Collectors.mapping(
                                MajorForGroup::getMajorName,
                                Collectors.toList()
                        )
                ));

        AtomicInteger counter = new AtomicInteger(1);

        return groupedMap.entrySet().stream()
                .map(entry -> MajorGroupResponse.builder()
                        .id(counter.getAndIncrement())
                        .majorGroupName(entry.getKey())
                        .majors(entry.getValue())
                        .numberOfMajors(entry.getValue().size())
                        .build())
                .collect(Collectors.toList());
    }



}
