package com.hhs.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserStatsVO {

    /**
     * 我发布的技巧数
     */
    private Integer publishCount;

    /**
     * 我的技巧总浏览量
     */
    private Integer totalViews;

    /**
     * 我的技巧获得的总点赞数
     */
    private Integer totalLikes;

    /**
     * 我收藏的技巧数
     */
    private Integer collectCount;

    /**
     * 我点赞的技巧数
     */
    private Integer likeCount;

    /**
     * 我发表的评论数
     */
    private Integer commentCount;
}

