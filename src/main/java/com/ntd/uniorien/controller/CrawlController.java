package com.ntd.uniorien.controller;

import com.ntd.uniorien.dto.response.ApiResponse;
import com.ntd.uniorien.service.CrawlService;
import com.ntd.uniorien.utils.helper.ResponseUtils;
import com.ntd.uniorien.utils.raw.SchoolInfo;
import com.ntd.uniorien.utils.raw.UniversityRawData;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/crawl")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CrawlController {

    CrawlService crawlService;

    @GetMapping("/uni-list")
    public ResponseEntity<ApiResponse<List<SchoolInfo>>> crawlUniversities() {
        List<SchoolInfo> schools = crawlService.crawlUniversities();
        return ResponseEntity.ok(ResponseUtils.ok(schools));
    }

    @GetMapping("/benchmarks")
    public ResponseEntity<ApiResponse<List<UniversityRawData>>> crawlBenchmarks() {
        List<UniversityRawData> universityRawData = crawlService.crawlBenchmarks();
        return ResponseEntity.ok(ResponseUtils.ok(universityRawData));
    }
}
