package com.hhs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hhs.entity.LikeRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface LikeRecordMapper extends BaseMapper<LikeRecord> {
    
    /**
     * 统计用户点赞的有效技巧数
     */
    @Select("SELECT COUNT(DISTINCT l.target_id) FROM like_record l " +
            "INNER JOIN health_tip t ON l.target_id = t.id " +
            "WHERE l.user_id = #{userId} AND l.target_type = 1 AND t.status = 1")
    Integer countValidLikesByUser(Long userId);
}
