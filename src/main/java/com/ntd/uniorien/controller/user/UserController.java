package com.ntd.uniorien.controller.user;

import com.ntd.uniorien.dto.request.UserCreation;
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
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(ResponseUtils.ok(users));
    }

    @PostMapping("/new-user")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@RequestBody @Valid UserCreation userRequest) {
        userService.createUser(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseUtils.created(null));
    }


}
