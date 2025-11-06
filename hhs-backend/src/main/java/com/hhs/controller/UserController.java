package com.hhs.controller;

import com.hhs.common.Result;
import com.hhs.dto.ChangePasswordRequest;
import com.hhs.dto.LoginRequest;
import com.hhs.dto.RegisterRequest;
import com.hhs.dto.UpdateProfileRequest;
import com.hhs.security.SecurityUtils;
import com.hhs.service.UserService;
import com.hhs.vo.AuthResponse;
import com.hhs.vo.UserProfileVO;
import com.hhs.vo.UserVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/auth/register")
    public Result<Void> register(@RequestBody @Valid RegisterRequest request) {
        userService.register(request);
        return Result.success();
    }

    @PostMapping("/auth/login")
    public Result<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        return Result.success(userService.login(request));
    }

    @GetMapping("/user/profile")
    public Result<UserProfileVO> profile() {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(userService.getUserProfile(userId));
    }

    @PutMapping("/user/profile")
    public Result<Void> updateProfile(@RequestBody @Valid UpdateProfileRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        userService.updateProfile(userId, request);
        return Result.success();
    }

    @PutMapping("/user/password")
    public Result<Void> changePassword(@RequestBody @Valid ChangePasswordRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        userService.changePassword(userId, request);
        return Result.success();
    }
}
