package com.hhs.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserStatsVO {

    private Integer publishCount;

    private Integer likeCount;

    private Integer collectCount;
}

