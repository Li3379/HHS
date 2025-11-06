package com.hhs.controller;

import com.hhs.common.Result;
import com.hhs.dto.CommentCreateRequest;
import com.hhs.security.SecurityUtils;
import com.hhs.service.CommentService;
import com.hhs.vo.CommentVO;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tips/{tipId}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public Result<List<CommentVO>> list(@PathVariable("tipId") Long tipId) {
        return Result.success(commentService.listComments(tipId));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Result<Void> create(@PathVariable("tipId") Long tipId,
                                @RequestBody @Valid CommentCreateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        commentService.createComment(userId, tipId, request);
        return Result.success();
    }
}




