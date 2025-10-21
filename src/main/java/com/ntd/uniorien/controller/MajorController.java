package com.ntd.uniorien.controller;

import com.ntd.uniorien.dto.response.ApiResponse;
import com.ntd.uniorien.dto.response.UserResponse;
import com.ntd.uniorien.service.BenchmarkService;
import com.ntd.uniorien.service.MajorService;
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
@RequestMapping("/api/v1/majors")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MajorController {

    MajorService majorService;

    @PostMapping(value = "/save-majors-groups", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> uploadBenchmarks(@RequestParam("file") MultipartFile file) {
        majorService.handleSaveMajorsAndMajorGroupsFromFileCSV(file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseUtils.created(null));
    }


    @GetMapping("/major-groups")
    public ResponseEntity<ApiResponse<?>> getMajorGroups() {



        return ResponseEntity.status(HttpStatus.OK).body(ResponseUtils.ok(majorService.getAllMajorGroups()));
    }


}
