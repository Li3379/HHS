package com.hhs.service;

import com.hhs.dto.CommentCreateRequest;
import com.hhs.vo.CommentVO;

import java.util.List;

public interface CommentService {

    List<CommentVO> listComments(Long tipId);

    void createComment(Long userId, Long tipId, CommentCreateRequest request);
}




