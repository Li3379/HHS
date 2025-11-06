package com.hhs.controller;

import com.hhs.common.Result;
import com.hhs.security.SecurityUtils;
import com.hhs.service.CommentService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comments")
public class CommentLikeController {

    private final CommentService commentService;

    public CommentLikeController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/{commentId}/like")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> like(@PathVariable("commentId") Long commentId) {
        Long userId = SecurityUtils.getCurrentUserId();
        commentService.likeComment(userId, commentId);
        return Result.success();
    }
}

