package com.ntd.uniorien.service;

import com.ntd.uniorien.constant.PredefinedRole;
import com.ntd.uniorien.dto.request.UserCreationRequest;
import com.ntd.uniorien.dto.response.CommentResponse;
import com.ntd.uniorien.dto.response.PageResponse;
import com.ntd.uniorien.dto.response.UserResponse;
import com.ntd.uniorien.entity.Comment;
import com.ntd.uniorien.entity.Role;
import com.ntd.uniorien.entity.University;
import com.ntd.uniorien.entity.User;
import com.ntd.uniorien.enums.ErrorCode;
import com.ntd.uniorien.exception.AppException;
import com.ntd.uniorien.repository.RoleRepository;
import com.ntd.uniorien.repository.UserRepository;
import com.ntd.uniorien.utils.helper.PageResponseUtil;
import com.ntd.uniorien.utils.mapper.UserMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;


@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {

    UserRepository userRepository;
    RoleRepository roleRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;

    public void createUser(UserCreationRequest userCreation) {
        if (userRepository.existsUserByEmail(userCreation.getEmail())) {
            throw new AppException(ErrorCode.USER_ALREADY_EXISTED);
        }

        User user = userMapper.toUser(userCreation);

        Role userRole = roleRepository.findRoleByRoleName(PredefinedRole.USER_ROLE)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        user.setRole(userRole);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        userRepository.save(user);
    }


    public PageResponse<UserResponse> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<User> userPage = userRepository.findAll(pageable);

        return PageResponseUtil.build(userPage, userMapper::toUserResponse);
    }


//    @PreAuthorize("hasRole('ADMIN')")
//    public List<UserResponse> getAllUsers() {
//        List<User> users = userRepository.findAll();
//        return users.stream().map(userMapper::toUserResponse).toList();
//    }

    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String email = context.getAuthentication().getName();
        User user = userRepository.findUserByEmail(email).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOTFOUND)
        );
        return userMapper.toUserResponse(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse getUserById(String id) {
        return userMapper.toUserResponse(userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOTFOUND)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(String id) {
        User user = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOTFOUND));
        userRepository.delete(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void lockUser(String id) {
        User user = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOTFOUND));
        user.setActive(false);
        userRepository.save(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void unlockUser(String id) {
        User user = userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOTFOUND));
        user.setActive(true);
        userRepository.save(user);
    }

}
