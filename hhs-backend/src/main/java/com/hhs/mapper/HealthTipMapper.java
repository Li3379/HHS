package com.hhs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hhs.entity.HealthTip;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface HealthTipMapper extends BaseMapper<HealthTip> {

    /**
     * 统计用户发布的技巧总浏览量
     */
    @Select("SELECT COALESCE(SUM(view_count), 0) FROM health_tip " +
            "WHERE user_id = #{userId} AND status = 1")
    Integer selectTotalViewsByUser(Long userId);

    /**
     * 统计用户发布的技巧获得的总点赞数
     */
    @Select("SELECT COALESCE(SUM(like_count), 0) FROM health_tip " +
            "WHERE user_id = #{userId} AND status = 1")
    Integer selectTotalLikesByUser(Long userId);
}
