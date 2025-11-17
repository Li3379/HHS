package com.hhs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hhs.common.ErrorCode;
import com.hhs.common.PageResult;
import com.hhs.dto.CommentCreateRequest;
import com.hhs.entity.Comment;
import com.hhs.entity.DeleteLog;
import com.hhs.entity.User;
import com.hhs.exception.BusinessException;
import com.hhs.mapper.CommentMapper;
import com.hhs.mapper.DeleteLogMapper;
import com.hhs.mapper.UserMapper;
import com.hhs.service.CommentService;
import com.hhs.vo.CommentVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final UserMapper userMapper;
    private final DeleteLogMapper deleteLogMapper;

    public CommentServiceImpl(CommentMapper commentMapper, UserMapper userMapper, DeleteLogMapper deleteLogMapper) {
        this.commentMapper = commentMapper;
        this.userMapper = userMapper;
        this.deleteLogMapper = deleteLogMapper;
    }

    @Override
    public PageResult<CommentVO> listComments(Long tipId, int page, int size) {
        int pageNum = Math.max(page, 1);
        int pageSize = Math.max(1, Math.min(size, 50));

        Page<Comment> pageRequest = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getTipId, tipId)
                .orderByDesc(Comment::getCreateTime);

        IPage<Comment> result = commentMapper.selectPage(pageRequest, wrapper);

        List<CommentVO> commentVOs = result.getRecords().stream().map(comment -> {
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

        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), commentVOs);
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
    
    @Override
    public void likeComment(Long userId, Long commentId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }
        
        // 使用UpdateWrapper只更新点赞数
        LambdaUpdateWrapper<Comment> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Comment::getId, commentId)
                .set(Comment::getLikeCount, comment.getLikeCount() + 1);
        commentMapper.update(null, updateWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long userId, Long commentId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "评论不存在");
        }
        
        // 权限校验：只能删除自己的评论
        if (!comment.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权删除他人的评论");
        }
        
        // 记录删除日志
        User operator = userMapper.selectById(userId);
        String contentSummary = comment.getContent().length() > 50 
                ? comment.getContent().substring(0, 50) + "..." 
                : comment.getContent();
        
        DeleteLog deleteLog = DeleteLog.builder()
                .operatorId(userId)
                .operatorName(operator != null ? operator.getNickname() : "未知用户")
                .targetType("COMMENT")
                .targetId(commentId)
                .targetTitle(contentSummary)
                .deleteTime(LocalDateTime.now())
                .remark("硬删除评论")
                .build();
        deleteLogMapper.insert(deleteLog);
        
        // 硬删除评论
        commentMapper.deleteById(commentId);
    }
}




