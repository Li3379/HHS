package com.hhs.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class TipDetailVO {

    private Long id;

    private String title;

    private String content;

    private String summary;

    private String category;

    private List<String> tags;

    private Long authorId;

    private String author;

    private String authorAvatar;

    private LocalDateTime publishTime;

    private Integer viewCount;

    private Integer likeCount;

    private Integer collectCount;

    private Boolean liked;

    private Boolean collected;
}
