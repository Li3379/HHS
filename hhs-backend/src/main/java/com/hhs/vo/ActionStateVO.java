package com.hhs.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ActionStateVO {

    private String action;

    private Boolean active;

    private Integer likeCount;

    private Integer collectCount;
}
