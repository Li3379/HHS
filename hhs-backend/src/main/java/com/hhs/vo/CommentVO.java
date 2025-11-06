package com.hhs.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CommentVO {

    private Long id;

    private Long userId;

    private String username;

    private String avatar;

    private String content;

    private Integer likeCount;

    private LocalDateTime createTime;

    private Long parentId;

    private Long tipId;

    private String tipTitle;
}




