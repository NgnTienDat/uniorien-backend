package com.ntd.uniorien.controller;

import com.nimbusds.jose.JOSEException;
import com.ntd.uniorien.dto.request.AuthenticationRequest;
import com.ntd.uniorien.dto.request.IntrospectRequest;
import com.ntd.uniorien.dto.request.LogoutRequest;
import com.ntd.uniorien.dto.request.RefreshRequest;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<Void> logout(@RequestBody LogoutRequest logoutRequest) throws ParseException, JOSEException {
        this.authenticationService.logout(logoutRequest);
        return ResponseUtils.buildResponse(null, "Logout", HttpStatus.OK);
    }

    @PostMapping("/introspect")
    public ResponseEntity<ApiResponse<IntrospectResponse>> introspect(
            @RequestBody @Valid IntrospectRequest introspectRequest) throws ParseException, JOSEException {

        IntrospectResponse result = authenticationService.introspect(introspectRequest);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtils.buildResponse(result, "Introspection Successful", HttpStatus.OK));

    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> refreshToken(
            @RequestBody RefreshRequest refreshRequest) throws ParseException, JOSEException {

        AuthenticationResponse result = authenticationService.refreshToken(refreshRequest);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtils.buildResponse(result, "Refresh token", HttpStatus.OK));
    }

    @PostMapping("/outbound/authentication")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> externalAuthenticate(
            @RequestParam("code") String code
    ){
        var result = authenticationService.outboundAuthenticate(code);


        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtils.buildResponse(
                        result,
                        "Outbound authentication successful",
                        HttpStatus.OK));

    }
}
