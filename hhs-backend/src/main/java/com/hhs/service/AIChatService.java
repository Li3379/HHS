package com.hhs.service;

import com.hhs.dto.ai.AIChatResponse;
import com.hhs.dto.ai.ConversationVO;

import java.util.List;

/**
 * AI对话服务
 */
public interface AIChatService {
    
    /**
     * AI对话
     * @param userId 用户ID（可为null）
     * @param sessionId 会话ID
     * @param question 问题
     * @return 回答
     */
    AIChatResponse chat(Long userId, String sessionId, String question);
    
    /**
     * 获取对话历史
     * @param sessionId 会话ID
     * @param userId 用户ID（可为null）
     * @return 历史记录列表
     */
    List<ConversationVO> getHistory(String sessionId, Long userId);
    
    /**
     * 清空会话
     * @param userId 用户ID
     * @param sessionId 会话ID
     */
    void clearSession(Long userId, String sessionId);
}
