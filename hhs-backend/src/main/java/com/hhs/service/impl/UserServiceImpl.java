package com.hhs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hhs.common.ErrorCode;
import com.hhs.dto.ChangePasswordRequest;
import com.hhs.dto.LoginRequest;
import com.hhs.dto.RegisterRequest;
import com.hhs.dto.UpdateProfileRequest;
import com.hhs.entity.User;
import com.hhs.exception.BusinessException;
import com.hhs.mapper.UserMapper;
import com.hhs.security.JwtUtil;
import com.hhs.service.UserService;
import com.hhs.vo.AuthResponse;
import com.hhs.vo.UserVO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterRequest request) {
        User exists = userMapper.selectOne(Wrappers.<User>lambdaQuery()
                .eq(User::getUsername, request.username())
                .last("LIMIT 1"));
        if (exists != null) {
            throw new BusinessException(ErrorCode.CONFLICT, "用户名已存在");
        }
        if (StringUtils.hasText(request.email())) {
            long count = userMapper.selectCount(Wrappers.<User>lambdaQuery()
                    .eq(User::getEmail, request.email()));
            if (count > 0) {
                throw new BusinessException(ErrorCode.CONFLICT, "邮箱已被注册");
            }
        }
        User user = new User();
        user.setUsername(request.username());
        user.setNickname(request.nickname());
        user.setEmail(request.email());
        user.setStatus(1);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setAvatar("https://api.dicebear.com/7.x/identicon/svg?seed=" + request.username());
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.insert(user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.username())
                .last("LIMIT 1"));
        if (user == null || user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "用户名或密码错误");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return AuthResponse.builder()
                .token(token)
                .userInfo(toVO(user))
                .build();
    }

    @Override
    public UserVO getProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        return toVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        if (StringUtils.hasText(request.email()) && !Objects.equals(request.email(), user.getEmail())) {
            long count = userMapper.selectCount(Wrappers.<User>lambdaQuery()
                    .eq(User::getEmail, request.email())
                    .ne(User::getId, userId));
            if (count > 0) {
                throw new BusinessException(ErrorCode.CONFLICT, "邮箱已被使用");
            }
            user.setEmail(request.email());
        }
        if (StringUtils.hasText(request.nickname())) {
            user.setNickname(request.nickname());
        }
        if (StringUtils.hasText(request.avatar())) {
            user.setAvatar(request.avatar());
        }
        if (StringUtils.hasText(request.phone())) {
            user.setPhone(request.phone());
        }
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "旧密码不正确");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    private UserVO toVO(User user) {
        return UserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .email(user.getEmail())
                .phone(user.getPhone())
                .build();
    }
}
