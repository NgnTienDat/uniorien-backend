package com.ntd.uniorien.controller;

import com.nimbusds.jose.JOSEException;
import com.ntd.uniorien.dto.request.AuthenticationRequest;
import com.ntd.uniorien.dto.request.IntrospectRequest;
import com.ntd.uniorien.dto.response.ApiResponse;
import com.ntd.uniorien.dto.response.AuthenticationResponse;
import com.ntd.uniorien.dto.response.IntrospectResponse;
import com.ntd.uniorien.service.AuthenticationService;
import com.ntd.uniorien.utils.helper.ResponseUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {

    AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<?>> authenticate(
            @RequestBody @Valid AuthenticationRequest authenticationRequest) {

        AuthenticationResponse result = authenticationService.authenticate(authenticationRequest);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtils.ok(result));
    }

    @PostMapping("/introspect")
    public ResponseEntity<ApiResponse<IntrospectResponse>> introspect(
            @RequestBody @Valid IntrospectRequest introspectRequest) throws ParseException, JOSEException {

        IntrospectResponse result = authenticationService.introspect(introspectRequest);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtils.buildResponse(result, "Introspection Successful", HttpStatus.OK));

    }
}
