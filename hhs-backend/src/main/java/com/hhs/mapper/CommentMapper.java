package com.hhs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hhs.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
    
    /**
     * 统计用户评论的有效技巧数
     */
    @Select("SELECT COUNT(DISTINCT c.id) FROM comment c " +
            "INNER JOIN health_tip t ON c.tip_id = t.id " +
            "WHERE c.user_id = #{userId} AND t.status = 1")
    Integer countValidCommentsByUser(Long userId);
}





















