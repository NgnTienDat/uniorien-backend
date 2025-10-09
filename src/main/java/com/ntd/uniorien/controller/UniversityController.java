package com.ntd.uniorien.controller;

import com.ntd.uniorien.service.CrawlService;
import com.ntd.uniorien.utils.raw.SchoolInfo;
import com.ntd.uniorien.dto.response.ApiResponse;
import com.ntd.uniorien.dto.response.UserResponse;
import com.ntd.uniorien.service.UniversityService;
import com.ntd.uniorien.utils.helper.ResponseUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/uni")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UniversityController {

    UniversityService universityService;
    CrawlService crawlService;


    @PostMapping("/save-list")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@RequestBody List<SchoolInfo> schoolInfoList) {
        universityService.saveUniversities(schoolInfoList);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseUtils.created(null));
    }


    @GetMapping("/")
    public ResponseEntity<ApiResponse<List<?>>> getAllUniversities() {
        return ResponseEntity.ok(ResponseUtils.ok(universityService.getAllUniversities()));
    }


    @GetMapping("/crawl/uni-list")
    public ResponseEntity<ApiResponse<List<SchoolInfo>>> crawlUniversities() {
        List<SchoolInfo> schools = crawlService.crawlUniversities();
        return ResponseEntity.ok(ResponseUtils.ok(schools));
    }

}
