package com.ntd.uniorien.service;

import com.ntd.uniorien.dto.request.UserCreation;
import com.ntd.uniorien.dto.response.UserResponse;
import com.ntd.uniorien.entity.User;
import com.ntd.uniorien.enums.ErrorCode;
import com.ntd.uniorien.exception.AppException;
import com.ntd.uniorien.repository.UserRepository;
import com.ntd.uniorien.utils.mapper.UserMapper;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Transactional
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {

    UserRepository userRepository;
    UserMapper userMapper;

    public void createUser(UserCreation userCreation) {
        if (userRepository.existsUserByEmail(userCreation.getEmail())) {
            throw new AppException(ErrorCode.USER_ALREADY_EXISTED);
        }

        User user = userMapper.toUser(userCreation);

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        userRepository.save(user);
    }


    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream().map(userMapper::toUserResponse).toList();
    }
}
