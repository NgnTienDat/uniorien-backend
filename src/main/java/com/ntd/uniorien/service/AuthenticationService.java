package com.ntd.uniorien.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.ntd.uniorien.constant.PredefinedRole;
import com.ntd.uniorien.dto.request.*;
import com.ntd.uniorien.dto.response.AuthenticationResponse;
import com.ntd.uniorien.dto.response.IntrospectResponse;
import com.ntd.uniorien.entity.InvalidatedToken;
import com.ntd.uniorien.entity.Role;
import com.ntd.uniorien.entity.User;
import com.ntd.uniorien.enums.ErrorCode;
import com.ntd.uniorien.exception.AppException;
import com.ntd.uniorien.repository.InvalidatedTokenRepository;
import com.ntd.uniorien.repository.RoleRepository;
import com.ntd.uniorien.repository.UserRepository;
import com.ntd.uniorien.repository.httpClient.OutboundIdentityClient;
import com.ntd.uniorien.repository.httpClient.OutboundUserClient;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashSet;
import java.util.StringJoiner;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthenticationService {
    UserRepository userRepository;
    RoleRepository roleRepository;
    InvalidatedTokenRepository invalidatedTokenRepository;
    OutboundIdentityClient outboundIdentityClient;
    OutboundUserClient outboundUserClient;

    @NonFinal
    @Value("${auth.signer-key}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${auth.valid-duration}")
    protected long VALID_DURATION;

    @NonFinal
    @Value("${auth.refreshable-duration}")
    protected long REFRESH_DURATION;

    @NonFinal
    @Value("${external.api.identity.client-id}")
    protected String CLIENT_ID;

    @NonFinal
    @Value("${external.api.identity.client-secret}")
    protected String CLIENT_SECRET;

    @NonFinal
    @Value("${external.api.identity.redirect-uri}")
    protected String REDIRECT_URI;

    @NonFinal
    protected String GRANT_TYPE = "authorization_code";

    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest) {
        User user = userRepository.findUserByEmail(authenticationRequest.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOTFOUND));

        if (!user.isActive()) throw new AppException(ErrorCode.ACCOUNT_LOCKED);

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        boolean matches = passwordEncoder.matches(authenticationRequest.getPassword(), user.getPassword());

        if (!matches) throw new AppException(ErrorCode.UNAUTHENTICATED);

        String token = generateToken(user);

        return AuthenticationResponse.builder()
                .authenticated(true)
                .token(token)
                .username(user.getFullName())
                .build();
    }

    public void logout(LogoutRequest logoutRequest) throws ParseException, JOSEException {
        try {
            SignedJWT signedToken = verifyToken(logoutRequest.getToken(), true);
            String jit = signedToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signedToken.getJWTClaimsSet().getExpirationTime();
            InvalidatedToken invalidatedToken = new InvalidatedToken(jit, expiryTime);
            invalidatedTokenRepository.save(invalidatedToken);
        } catch (AppException e) {
            log.info("Token is expired or invalid during logout");
        }
    }

    private String generateToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getEmail())
                .issuer("uniorien.vn")
                .issueTime(new Date())
                .jwtID(UUID.randomUUID().toString())
                .expirationTime(new Date(Instant.now().plus(VALID_DURATION, ChronoUnit.DAYS).toEpochMilli()))
                .claim("scope", buildScope(user))
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);
        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create JWT token", e);
            throw new RuntimeException(e);
        }
    }


    public IntrospectResponse introspect(IntrospectRequest request) throws JOSEException, ParseException {
        String token = request.getToken();
        boolean isValid = true;
        try {
            verifyToken(token, false);
        } catch (AppException e) {
            isValid = false;
        }
        return IntrospectResponse.builder()
                .valid(isValid)
                .build();
    }

    private String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");
        if (!CollectionUtils.isEmpty(user.getRoles())) {
            user.getRoles().forEach(role -> stringJoiner.add("ROLE_" + role.getRoleName()));
        }
        return stringJoiner.toString();
    }

    private SignedJWT verifyToken(String token, boolean isRefresh) throws ParseException, JOSEException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiration = isRefresh
                ? new Date(signedJWT
                .getJWTClaimsSet()
                .getIssueTime().toInstant()
                .plus(REFRESH_DURATION, ChronoUnit.DAYS)
                .toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime();

        if (!(signedJWT.verify(verifier) && expiration.after(new Date()))) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        if (invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return signedJWT;
    }

    public AuthenticationResponse refreshToken(RefreshRequest refreshRequest) throws ParseException, JOSEException {
        SignedJWT signedJWT = verifyToken(refreshRequest.getToken(), true);

        String jit = signedJWT.getJWTClaimsSet().getJWTID();
        Date expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();

        // Kiểm tra xem token đã được invalidate trước đó chưa
        if (invalidatedTokenRepository.existsById(jit)) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        String email = signedJWT.getJWTClaimsSet().getSubject();
        User user = userRepository
                .findUserByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        String newToken = generateToken(user);
        InvalidatedToken invalidatedToken = new InvalidatedToken(jit, expiryTime);
        invalidatedTokenRepository.save(invalidatedToken);

        return AuthenticationResponse.builder()
                .token(newToken)
                .username(user.getFullName())
                .authenticated(true)
                .build();
    }

    @Transactional
    public AuthenticationResponse outboundAuthenticate(String code) {
        var response = outboundIdentityClient.exchangeToken(ExchangeTokenRequest.builder()
                .code(code)
                .clientId(CLIENT_ID)
                .clientSecret(CLIENT_SECRET)
                .redirectUri(REDIRECT_URI)
                .grantType(GRANT_TYPE)
                .build());

        var userInfo = outboundUserClient.getUserInfo("json", response.getAccessToken());

        HashSet<Role> roles = new HashSet<>();
        roleRepository.findRoleByRoleName(PredefinedRole.USER_ROLE).ifPresent(roles::add);

//        HashSet<String> roles = new HashSet<>();
//        roles.add(Role.USER.name());

        User user = userRepository.findUserByEmail(userInfo.getEmail())
                .orElseGet(() -> {
                    try {
                        User user1 = User.builder()
                                .email(userInfo.getEmail())
                                .fullName(userInfo.getName())
                                .avatar(userInfo.getPicture())
                                .roles(roles)
                                .build();

                        userRepository.save(user1);
                        return user1;
                    } catch (DataIntegrityViolationException ex) {

                        return userRepository.findUserByEmail(userInfo.getEmail())
                                .orElseThrow();
                    }
                });

        String token = generateToken(user);
        return AuthenticationResponse.builder().authenticated(true).token(token).username(user.getFullName()).build();
    }


}
