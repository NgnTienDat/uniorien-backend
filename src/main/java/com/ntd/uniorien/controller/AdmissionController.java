package com.ntd.uniorien.controller;

import com.ntd.uniorien.dto.response.ApiResponse;
import com.ntd.uniorien.dto.response.UserResponse;
import com.ntd.uniorien.service.BenchmarkService;
import com.ntd.uniorien.service.UniversityService;
import com.ntd.uniorien.utils.helper.ResponseUtils;
import com.ntd.uniorien.utils.raw.SchoolInfo;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/admissions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdmissionController {

    UniversityService universityService;
    BenchmarkService benchmarkService;

    @DeleteMapping("/delete-all")
    public ResponseEntity<ApiResponse<?>> deleteUniversities() {
        universityService.deleteUniversities();
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseUtils.ok(null));
    }


    @PostMapping("/save-list")
    public ResponseEntity<ApiResponse<UserResponse>> createUniversities(@RequestBody List<SchoolInfo> schoolInfoList) {
        universityService.saveUniversities(schoolInfoList);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseUtils.created(null));
    }


    @PostMapping(value = "/save-benchmarks", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> uploadBenchmarks(@RequestParam("file") MultipartFile file) {
        benchmarkService.handleSaveBenchmarkFromFileCSV(file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseUtils.created(null));
    }


    @GetMapping("/")
    public ResponseEntity<ApiResponse<List<?>>> getAllUniversities() {
        return ResponseEntity.ok(ResponseUtils.ok(universityService.getAllUniversities()));
    }

    @GetMapping("/benchmarks/{universityCode}")
    public ResponseEntity<ApiResponse<?>> getBenchmarksByUniversityCode(
            @PathVariable(value = "universityCode") String universityCode,
            @RequestParam(value = "year", required = false) String year,
            @RequestParam(value = "admissionMethod", required = false) String admissionMethod
    ) {
        System.out.println("year "+year);
        System.out.println("universityCode "+universityCode);
        System.out.println("admissionMethod "+admissionMethod);

        return ResponseEntity.ok(ResponseUtils.ok(universityService
                .getAdmissionsByUniversityCode(universityCode, year, admissionMethod)));
    }

}
