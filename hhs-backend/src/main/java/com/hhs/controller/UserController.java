package com.hhs.controller;

import com.hhs.common.PageResult;
import com.hhs.common.Result;
import com.hhs.dto.ChangePasswordRequest;
import com.hhs.dto.LoginRequest;
import com.hhs.dto.RegisterRequest;
import com.hhs.dto.UpdateProfileRequest;
import com.hhs.security.SecurityUtils;
import com.hhs.service.HealthTipService;
import com.hhs.service.UserService;
import com.hhs.vo.AuthResponse;
import com.hhs.vo.CommentVO;
import com.hhs.vo.TipListItemVO;
import com.hhs.vo.UserProfileVO;
import com.hhs.vo.UserPublicProfileVO;
import com.hhs.vo.UserVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;
    private final HealthTipService healthTipService;

    public UserController(UserService userService, HealthTipService healthTipService) {
        this.userService = userService;
        this.healthTipService = healthTipService;
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

    @GetMapping("/users/{id}")
    public Result<UserPublicProfileVO> publicProfile(@PathVariable("id") Long userId) {
        return Result.success(userService.getPublicProfile(userId));
    }

    @GetMapping("/users/{id}/tips")
    public Result<PageResult<TipListItemVO>> publicTips(@PathVariable("id") Long userId,
                                                        @RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "10") int size) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        return Result.success(healthTipService.pageUserTips(currentUserId, userId, page, size));
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

    @GetMapping("/user/tips")
    public Result<PageResult<TipListItemVO>> myTips(@RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "10") int size) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(userService.pageUserTips(userId, userId, page, size));
    }

    @GetMapping("/user/collects")
    public Result<PageResult<TipListItemVO>> myCollects(@RequestParam(defaultValue = "1") int page,
                                                          @RequestParam(defaultValue = "10") int size) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(userService.pageUserCollects(userId, page, size));
    }

    @GetMapping("/user/likes")
    public Result<PageResult<TipListItemVO>> myLikes(@RequestParam(defaultValue = "1") int page,
                                                       @RequestParam(defaultValue = "10") int size) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(userService.pageUserLikes(userId, page, size));
    }

    @GetMapping("/user/comments")
    public Result<PageResult<CommentVO>> myComments(@RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "10") int size) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(userService.pageUserComments(userId, page, size));
    }
}
