package com.ntd.uniorien.service;

import com.ntd.uniorien.utils.raw.AdmissionInfoRawData;
import com.ntd.uniorien.utils.raw.BenchmarkRawData;
import com.ntd.uniorien.utils.raw.MajorGroupRawData;
import com.ntd.uniorien.utils.raw.UniversityRawData;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CsvExportService {

    public void exportToCsv(List<UniversityRawData> universities, String filePath) {
        File file = new File(filePath);
        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }

        try (OutputStream os = new FileOutputStream(file);
             OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8);
             BufferedWriter writer = new BufferedWriter(osw)) {

            // Ghi BOM UTF-8 để Excel trên Windows nhận diện đúng
            writer.write('\uFEFF');

            // Header
            writer.write(String.join(",",
                    "University Code",
                    "University Name",
                    "Website",
                    "Year",
                    "Admission Method",
                    "Major Code",
                    "Major Name",
                    "Subject Combinations",
                    "Score",
                    "Notes"));
            writer.newLine();

            // Body
            for (UniversityRawData u : universities) {
                if (u.getAdmissions() == null) continue;
                for (AdmissionInfoRawData admission : u.getAdmissions()) {
                    if (admission.getBenchmarks() == null) continue;
                    for (BenchmarkRawData b : admission.getBenchmarks()) {
                        writer.write(joinCsv(
                                u.getUniversityCode(),
                                u.getUniversityName(),
                                u.getWebsiteUrl(),
                                admission.getYear(),
                                admission.getAdmissionMethod(),
                                b.getMajorCode(),
                                b.getMajorName(),
                                b.getSubjectCombinations(),
                                b.getScore(),
                                b.getNotes()
                        ));
                        writer.newLine();
                    }
                }
            }

            writer.flush();
            System.out.println("Successful! CSV exported to: " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void exportMajorGroupsToCsv(List<MajorGroupRawData> groups, String filePath) {
        File file = new File(filePath);
        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }

        try (OutputStream os = new FileOutputStream(file);
             OutputStreamWriter osw = new OutputStreamWriter(os, StandardCharsets.UTF_8);
             BufferedWriter writer = new BufferedWriter(osw)) {

            // Ghi BOM UTF-8
            writer.write('\uFEFF');

            // Header
            writer.write(String.join(",", "Group Name", "Number of Majors", "Majors"));
            writer.newLine();

            // Body
            for (MajorGroupRawData g : groups) {
                writer.write(joinCsv(
                        g.getGroupName(),
                        g.getNumberOfMajors() != null ? g.getNumberOfMajors().toString() : "",
                        g.getMajors() != null ? String.join("; ", g.getMajors()) : ""
                ));
                writer.newLine();
            }

            writer.flush();
            log.info("CSV exported successfully: {}", file.getAbsolutePath());

        } catch (IOException e) {
            log.error("Error exporting major groups CSV: {}", e.getMessage(), e);
        }
    }


    private static String joinCsv(String... cols) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cols.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(escapeCsv(cols[i]));
        }
        return sb.toString();
    }

    private static String escapeCsv(String value) {
        if (value == null) return "";
        String v = value.replace("\"", "\"\"");
        if (v.contains(",") || v.contains("\"") || v.contains("\n") || v.contains("\r")) {
            return "\"" + v + "\"";
        } else {
            return v;
        }
    }
}
