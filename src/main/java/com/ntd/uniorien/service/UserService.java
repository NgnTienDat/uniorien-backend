package com.ntd.uniorien.service;

import com.ntd.uniorien.constant.PredefinedRole;
import com.ntd.uniorien.dto.request.UserCreationRequest;
import com.ntd.uniorien.dto.response.UserResponse;
import com.ntd.uniorien.entity.Role;
import com.ntd.uniorien.entity.User;
import com.ntd.uniorien.enums.ErrorCode;
import com.ntd.uniorien.exception.AppException;
import com.ntd.uniorien.repository.RoleRepository;
import com.ntd.uniorien.repository.UserRepository;
import com.ntd.uniorien.utils.mapper.UserMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
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
        HashSet<Role> roles = new HashSet<>();
        roleRepository.findRoleByRoleName(PredefinedRole.USER_ROLE).ifPresent(roles::add);
        user.setRoles(roles);

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        userRepository.save(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream().map(userMapper::toUserResponse).toList();
    }
}
