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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用户模块", description = "用户认证、个人中心相关接口")
@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;
    private final HealthTipService healthTipService;

    public UserController(UserService userService, HealthTipService healthTipService) {
        this.userService = userService;
        this.healthTipService = healthTipService;
    }

    @Operation(summary = "用户注册", description = "创建新用户账号")
    @PostMapping("/auth/register")
    public Result<Void> register(@RequestBody @Valid RegisterRequest request) {
        userService.register(request);
        return Result.success();
    }

    @Operation(summary = "用户登录", description = "通过用户名密码登录，返回 JWT Token")
    @PostMapping("/auth/login")
    public Result<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        return Result.success(userService.login(request));
    }

    @Operation(summary = "查看用户信息", description = "获取指定用户的公开信息（无需登录）")
    @GetMapping("/users/{id}")
    public Result<UserPublicProfileVO> publicProfile(
            @Parameter(description = "用户ID", required = true, example = "1") 
            @PathVariable("id") Long userId) {
        return Result.success(userService.getPublicProfile(userId));
    }

    @Operation(summary = "查看用户发布", description = "获取指定用户发布的技巧列表（无需登录）")
    @GetMapping("/users/{id}/tips")
    public Result<PageResult<TipListItemVO>> publicTips(
            @Parameter(description = "用户ID", required = true, example = "1") 
            @PathVariable("id") Long userId,
                                                        @RequestParam(defaultValue = "1") int page,
                                                        @RequestParam(defaultValue = "10") int size) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        return Result.success(healthTipService.pageUserTips(currentUserId, userId, page, size));
    }

    @Operation(summary = "获取个人资料", description = "获取当前登录用户的详细资料")
    @GetMapping("/user/profile")
    public Result<UserProfileVO> profile() {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(userService.getUserProfile(userId));
    }

    @Operation(summary = "修改个人资料", description = "更新当前用户的昵称、邮箱等信息")
    @PutMapping("/user/profile")
    public Result<Void> updateProfile(@RequestBody @Valid UpdateProfileRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        userService.updateProfile(userId, request);
        return Result.success();
    }

    @Operation(summary = "修改密码", description = "修改当前用户的登录密码")
    @PutMapping("/user/password")
    public Result<Void> changePassword(@RequestBody @Valid ChangePasswordRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        userService.changePassword(userId, request);
        return Result.success();
    }

    @Operation(summary = "我的发布", description = "获取当前用户发布的技巧列表")
    @GetMapping("/user/tips")
    public Result<PageResult<TipListItemVO>> myTips(@RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "10") int size) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(userService.pageUserTips(userId, userId, page, size));
    }

    @Operation(summary = "我的收藏", description = "获取当前用户收藏的技巧列表")
    @GetMapping("/user/collects")
    public Result<PageResult<TipListItemVO>> myCollects(@RequestParam(defaultValue = "1") int page,
                                                          @RequestParam(defaultValue = "10") int size) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(userService.pageUserCollects(userId, page, size));
    }

    @Operation(summary = "我的点赞", description = "获取当前用户点赞的技巧列表")
    @GetMapping("/user/likes")
    public Result<PageResult<TipListItemVO>> myLikes(@RequestParam(defaultValue = "1") int page,
                                                       @RequestParam(defaultValue = "10") int size) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(userService.pageUserLikes(userId, page, size));
    }

    @Operation(summary = "我的评论", description = "获取当前用户发表的评论列表")
    @GetMapping("/user/comments")
    public Result<PageResult<CommentVO>> myComments(@RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "10") int size) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(userService.pageUserComments(userId, page, size));
    }
}
