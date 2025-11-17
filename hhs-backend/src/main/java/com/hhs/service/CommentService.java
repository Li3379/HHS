package com.hhs.service;

import com.hhs.common.PageResult;
import com.hhs.dto.CommentCreateRequest;
import com.hhs.vo.CommentVO;

import java.util.List;

public interface CommentService {

    PageResult<CommentVO> listComments(Long tipId, int page, int size);

    void createComment(Long userId, Long tipId, CommentCreateRequest request);
    
    void likeComment(Long userId, Long commentId);

    void deleteComment(Long userId, Long commentId);
}




