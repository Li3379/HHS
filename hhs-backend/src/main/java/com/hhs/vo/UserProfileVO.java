package com.hhs.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UserProfileVO {

    private UserVO profile;

    private UserStatsVO stats;

    private List<TipListItemVO> publishList;

    private List<TipListItemVO> collectList;

    private List<CommentVO> commentList;
}

