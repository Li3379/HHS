package com.hhs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hhs.entity.Collect;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CollectMapper extends BaseMapper<Collect> {
    
    /**
     * 统计用户收藏的有效技巧数
     */
    @Select("SELECT COUNT(DISTINCT c.tip_id) FROM collect c " +
            "INNER JOIN health_tip t ON c.tip_id = t.id " +
            "WHERE c.user_id = #{userId} AND t.status = 1")
    Integer countValidCollectsByUser(Long userId);
}
