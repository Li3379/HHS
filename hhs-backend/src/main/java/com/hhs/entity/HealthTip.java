package com.hhs.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("health_tip")
public class HealthTip {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String title;

    private String content;

    private String summary;

    private String category;

    private String tags;

    private Integer viewCount;

    private Integer likeCount;

    private Integer collectCount;

    private Integer status;

    private LocalDateTime publishTime;

    private LocalDateTime updateTime;
}
