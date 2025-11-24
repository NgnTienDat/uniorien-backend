package com.ntd.uniorien.controller;

import com.ntd.uniorien.dto.request.UserCreationRequest;
import com.ntd.uniorien.dto.response.ApiResponse;
import com.ntd.uniorien.dto.response.UserResponse;
import com.ntd.uniorien.service.UserService;
import com.ntd.uniorien.utils.helper.ResponseUtils;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserController {

    UserService userService;

    @GetMapping("/")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<?>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size
    ) {
        return ResponseEntity.ok(ResponseUtils.ok(userService.getAllUsers(page, size)));
    }

    @PostMapping("/new-user")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@RequestBody @Valid UserCreationRequest userRequest) {
        userService.createUser(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseUtils.created(null));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable String userId) {
        return ResponseEntity.ok(ResponseUtils.ok(userService.getUserById(userId)));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable("userId") String userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok(ResponseUtils.ok(null));

    }

    @PostMapping("/{userId}/lock")
    public ResponseEntity<ApiResponse<Void>> lockUser(@PathVariable("userId") String userId) {
        userService.lockUser(userId);
        return ResponseEntity.ok(ResponseUtils.ok(null));
    }

    @PostMapping("/{userId}/unlock")
    public ResponseEntity<ApiResponse<Void>> unlockUser(@PathVariable("userId") String userId) {
        userService.unlockUser(userId);
        return ResponseEntity.ok(ResponseUtils.ok(null));

    }

    @GetMapping("/my-info")
    public ResponseEntity<ApiResponse<UserResponse>> getMyInfo() {
        return ResponseEntity.ok(ResponseUtils.ok(userService.getMyInfo()));
    }
}
