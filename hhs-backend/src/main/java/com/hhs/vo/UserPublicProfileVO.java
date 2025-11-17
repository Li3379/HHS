package com.hhs.vo;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserPublicProfileVO {

    private Long id;

    private String nickname;

    private String avatar;

    private UserStatsVO stats;

    private List<TipListItemVO> recentTips;
}
