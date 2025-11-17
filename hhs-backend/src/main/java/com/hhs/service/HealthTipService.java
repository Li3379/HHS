package com.hhs.service;

import com.hhs.common.PageResult;
import com.hhs.dto.TipCreateRequest;
import com.hhs.dto.TipPageQuery;
import com.hhs.vo.ActionStateVO;
import com.hhs.vo.TipDetailVO;
import com.hhs.vo.TipListItemVO;

public interface HealthTipService {

    void createTip(Long userId, TipCreateRequest request);

    PageResult<TipListItemVO> pageTips(Long userId, TipPageQuery query);

    TipDetailVO getTipDetail(Long userId, Long tipId);

    ActionStateVO likeTip(Long userId, Long tipId);

    ActionStateVO collectTip(Long userId, Long tipId);

    PageResult<TipListItemVO> pageUserTips(Long currentUserId, Long authorId, int page, int size);

    void deleteTip(Long userId, Long tipId);
}
