package com.hhs.controller;

import com.hhs.common.PageResult;
import com.hhs.common.Result;
import com.hhs.dto.CommentCreateRequest;
import com.hhs.security.SecurityUtils;
import com.hhs.service.CommentService;
import com.hhs.vo.CommentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "评论模块", description = "发表评论、点赞评论、删除评论")
@RestController
@RequestMapping("/api/tips/{tipId}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @Operation(summary = "评论列表", description = "获取指定技巧的评论列表（无需登录）")
    @GetMapping
    public Result<PageResult<CommentVO>> list(
            @Parameter(description = "技巧ID", required = true, example = "1") 
            @PathVariable("tipId") Long tipId,
                                               @RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "20") int size) {
        return Result.success(commentService.listComments(tipId, page, size));
    }

    @Operation(summary = "发表评论", description = "对指定技巧发表评论")
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Result<Void> create(
            @Parameter(description = "技巧ID", required = true, example = "1") 
            @PathVariable("tipId") Long tipId,
                                @RequestBody @Valid CommentCreateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        commentService.createComment(userId, tipId, request);
        return Result.success();
    }

    @Operation(summary = "删除评论", description = "删除自己发表的评论")
    @DeleteMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> delete(
            @Parameter(description = "技巧ID", required = true, example = "1") 
            @PathVariable("tipId") Long tipId,
            @Parameter(description = "评论ID", required = true, example = "1") 
            @PathVariable("commentId") Long commentId) {
        Long userId = SecurityUtils.getCurrentUserId();
        commentService.deleteComment(userId, commentId);
        return Result.success();
    }

    @Operation(summary = "点赞评论", description = "对指定评论点赞或取消点赞")
    @PostMapping("/{commentId}/like")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> like(
            @Parameter(description = "技巧ID", required = true, example = "1") 
            @PathVariable("tipId") Long tipId,
            @Parameter(description = "评论ID", required = true, example = "1") 
            @PathVariable("commentId") Long commentId) {
        Long userId = SecurityUtils.getCurrentUserId();
        commentService.likeComment(userId, commentId);
        return Result.success();
    }
}




