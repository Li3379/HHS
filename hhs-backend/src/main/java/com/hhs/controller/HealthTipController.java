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
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Result<Void> create(@RequestBody @Valid TipCreateRequest request) {
        Long userId = requireLoginUserId();
        healthTipService.createTip(userId, request);
        return Result.success();
    }

    @GetMapping
    public Result<PageResult<TipListItemVO>> list(@Valid TipPageQuery query) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(healthTipService.pageTips(userId, query));
    }

    @GetMapping("/{id}")
    public Result<TipDetailVO> detail(@PathVariable("id") Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.success(healthTipService.getTipDetail(userId, id));
    }

    @PostMapping("/{id}/like")
    @PreAuthorize("isAuthenticated()")
    public Result<ActionStateVO> like(@PathVariable("id") Long id) {
        Long userId = requireLoginUserId();
        return Result.success(healthTipService.likeTip(userId, id));
    }

    @PostMapping("/{id}/collect")
    @PreAuthorize("isAuthenticated()")
    public Result<ActionStateVO> collect(@PathVariable("id") Long id) {
        Long userId = requireLoginUserId();
        return Result.success(healthTipService.collectTip(userId, id));
    }
}
