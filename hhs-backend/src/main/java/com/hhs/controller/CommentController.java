package com.hhs.controller;

import com.hhs.common.PageResult;
import com.hhs.common.Result;
import com.hhs.dto.CommentCreateRequest;
import com.hhs.security.SecurityUtils;
import com.hhs.service.CommentService;
import com.hhs.vo.CommentVO;
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

@RestController
@RequestMapping("/api/tips/{tipId}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public Result<PageResult<CommentVO>> list(@PathVariable("tipId") Long tipId,
                                               @RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "20") int size) {
        return Result.success(commentService.listComments(tipId, page, size));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Result<Void> create(@PathVariable("tipId") Long tipId,
                                @RequestBody @Valid CommentCreateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        commentService.createComment(userId, tipId, request);
        return Result.success();
    }

    @DeleteMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> delete(@PathVariable("tipId") Long tipId,
                               @PathVariable("commentId") Long commentId) {
        Long userId = SecurityUtils.getCurrentUserId();
        commentService.deleteComment(userId, commentId);
        return Result.success();
    }

    @PostMapping("/{commentId}/like")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> like(@PathVariable("tipId") Long tipId,
                              @PathVariable("commentId") Long commentId) {
        Long userId = SecurityUtils.getCurrentUserId();
        commentService.likeComment(userId, commentId);
        return Result.success();
    }
}




