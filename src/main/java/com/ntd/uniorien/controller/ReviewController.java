package com.ntd.uniorien.controller;

import com.ntd.uniorien.dto.request.CommentCreationRequest;
import com.ntd.uniorien.dto.response.ApiResponse;
import com.ntd.uniorien.dto.response.UserResponse;
import com.ntd.uniorien.service.BenchmarkService;
import com.ntd.uniorien.service.ReviewService;
import com.ntd.uniorien.service.UniversityService;
import com.ntd.uniorien.utils.helper.ResponseUtils;
import com.ntd.uniorien.utils.raw.SchoolInfo;
import jakarta.validation.Valid;
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
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReviewController {

    ReviewService reviewService;



    @GetMapping("/")
    public ResponseEntity<ApiResponse<List<?>>> getAllUniversities() {
        return ResponseEntity.ok(ResponseUtils.ok(reviewService.allUniversityReviews()));
    }

    @GetMapping("/{universityCode}")
    public ResponseEntity<ApiResponse<?>> getUniversityDetailInfo(
            @PathVariable(value = "universityCode") String universityCode
    ) {
        return ResponseEntity.ok(ResponseUtils.ok(reviewService.getDetailInfo(universityCode)));
    }

    @GetMapping("/{universityCode}/comments")
    public ResponseEntity<ApiResponse<List<?>>> getAllComments(
            @PathVariable(value = "universityCode") String universityCode) {
        return ResponseEntity.ok(ResponseUtils.ok(reviewService.getAllComments(universityCode)));
    }



    @PostMapping("/comments")
    public ResponseEntity<ApiResponse<?>> addComment(
            @RequestBody @Valid CommentCreationRequest commentCreationRequest) {
        reviewService.createComment(commentCreationRequest);
        return ResponseEntity.ok(ResponseUtils.created(null));

    }









}
