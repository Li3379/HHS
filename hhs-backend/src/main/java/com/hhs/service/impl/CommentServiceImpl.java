package com.hhs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hhs.dto.CommentCreateRequest;
import com.hhs.entity.Comment;
import com.hhs.entity.User;
import com.hhs.mapper.CommentMapper;
import com.hhs.mapper.UserMapper;
import com.hhs.service.CommentService;
import com.hhs.vo.CommentVO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final UserMapper userMapper;

    public CommentServiceImpl(CommentMapper commentMapper, UserMapper userMapper) {
        this.commentMapper = commentMapper;
        this.userMapper = userMapper;
    }

    @Override
    public List<CommentVO> listComments(Long tipId) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getTipId, tipId)
                .orderByDesc(Comment::getCreateTime);

        List<Comment> comments = commentMapper.selectList(wrapper);

        return comments.stream().map(comment -> {
            User user = userMapper.selectById(comment.getUserId());
            return CommentVO.builder()
                    .id(comment.getId())
                    .userId(comment.getUserId())
                    .username(user != null ? user.getNickname() : "未知用户")
                    .avatar(user != null ? user.getAvatar() : null)
                    .content(comment.getContent())
                    .likeCount(comment.getLikeCount())
                    .createTime(comment.getCreateTime())
                    .parentId(comment.getParentId())
                    .build();
        }).toList();
    }

    @Override
    public void createComment(Long userId, Long tipId, CommentCreateRequest request) {
        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setTipId(tipId);
        comment.setContent(request.content());
        comment.setParentId(request.parentId() != null ? request.parentId() : 0L);
        comment.setLikeCount(0);
        comment.setCreateTime(LocalDateTime.now());

        commentMapper.insert(comment);
    }
}




