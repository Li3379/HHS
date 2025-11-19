package com.hhs.controller;

import com.hhs.common.ErrorCode;
import com.hhs.common.PageResult;
import com.hhs.common.Result;
import com.hhs.dto.TipCreateRequest;
import com.hhs.dto.TipPageQuery;
import com.hhs.exception.BusinessException;
import com.hhs.security.SecurityUtils;
import com.hhs.service.HealthTipService;
import com.hhs.vo.ActionStateVO;
import com.hhs.vo.TipDetailVO;
import com.hhs.vo.TipListItemVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "技巧模块", description = "健康技巧的发布、浏览、点赞、收藏等功能")
@RestController
@RequestMapping("/api/tips")
@Validated
public class HealthTipController {

    private final HealthTipService healthTipService;

    public HealthTipController(HealthTipService healthTipService) {
        this.healthTipService = healthTipService;
    }

    private Long requireLoginUserId() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "未登录或登录已过期");
        }
        return userId;
    }

    @Operation(summary = "发布技巧", description = "创建一个新的健康生活技巧")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Result<Void> create(@RequestBody @Valid TipCreateRequest request) {
        Long userId = requireLoginUserId();
        healthTipService.createTip(userId, request);
        return Result.success();
    }

    @Operation(summary = "技巧列表", description = "分页获取技巧列表，支持按分类、关键词搜索（无需登录）")
    @GetMapping
    public Result<PageResult<TipListItemVO>> list(@Valid TipPageQuery query) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(healthTipService.pageTips(userId, query));
    }

    @Operation(summary = "技巧详情", description = "获取技巧的详细内容（无需登录）")
    @GetMapping("/{id}")
    public Result<TipDetailVO> detail(
            @Parameter(description = "技巧ID", required = true, example = "1") 
            @PathVariable("id") Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(healthTipService.getTipDetail(userId, id));
    }

    @Operation(summary = "点赞技巧", description = "对指定技巧点赞或取消点赞")
    @PostMapping("/{id}/like")
    @PreAuthorize("isAuthenticated()")
    public Result<ActionStateVO> like(
            @Parameter(description = "技巧ID", required = true, example = "1") 
            @PathVariable("id") Long id) {
        Long userId = requireLoginUserId();
        return Result.success(healthTipService.likeTip(userId, id));
    }

    @Operation(summary = "收藏技巧", description = "收藏或取消收藏指定技巧")
    @PostMapping("/{id}/collect")
    @PreAuthorize("isAuthenticated()")
    public Result<ActionStateVO> collect(
            @Parameter(description = "技巧ID", required = true, example = "1") 
            @PathVariable("id") Long id) {
        Long userId = requireLoginUserId();
        return Result.success(healthTipService.collectTip(userId, id));
    }

    @Operation(summary = "删除技巧", description = "删除自己发布的技巧")
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> delete(
            @Parameter(description = "技巧ID", required = true, example = "1") 
            @PathVariable("id") Long id) {
        Long userId = requireLoginUserId();
        healthTipService.deleteTip(userId, id);
        return Result.success();
    }
}
