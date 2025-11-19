package com.hhs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hhs.common.ErrorCode;
import com.hhs.component.AIRateLimiter;
import com.hhs.component.ContentFilter;
import com.hhs.dto.ai.AIChatResponse;
import com.hhs.dto.ai.ConversationVO;
import com.hhs.entity.AIConversation;
import com.hhs.exception.BusinessException;
import com.hhs.mapper.AIConversationMapper;
import com.hhs.service.AIChatService;
import dev.langchain4j.data.message.*;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * AI对话服务实现
 */
@Slf4j
@Service
public class AIChatServiceImpl implements AIChatService {
    
    @Autowired
    private ChatLanguageModel chatModel;
    
    @Autowired
    private AIConversationMapper conversationMapper;
    
    @Autowired
    private AIRateLimiter rateLimiter;
    
    @Autowired
    private ContentFilter contentFilter;
    
    private static final String SYSTEM_PROMPT = """
        你是"小健"，HHS平台的AI健康顾问助手。
        
        你的职责：
        1. 提供准确、专业、易懂的健康建议
        2. 基于科学依据，不传播伪科学
        3. 语气友好、专业、鼓励性
        4. 回答简洁，控制在200字以内
        
        重要原则：
        - 不提供疾病诊断（建议就医）
        - 不推荐具体药品
        - 强调个体差异
        - 涉及严重健康问题时，建议咨询专业医生
        
        你擅长的领域：
        - 饮食营养建议
        - 运动健身指导
        - 睡眠质量改善
        - 心理健康调节
        - 日常保健知识
        """;
    
    @Override
    @Transactional
    public AIChatResponse chat(Long userId, String sessionId, String question) {
        log.info("AI对话请求: userId={}, sessionId={}, question={}", 
            userId, sessionId, question);
        
        // 1. 限流检查
        if (!rateLimiter.checkLimit(userId)) {
            String message = userId != null 
                ? "今日咨询次数已用完（20次），请明天再来" 
                : "访客每天只能咨询3次，登录后可享更多次数";
            throw new BusinessException(ErrorCode.RATE_LIMIT_EXCEEDED, message);
        }
        
        // 2. 内容安全检查
        String cleanQuestion = contentFilter.cleanInput(question);
        if (contentFilter.containsSensitive(cleanQuestion)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, 
                "问题包含敏感内容，请重新表述");
        }
        
        // 3. 获取对话历史（最近3轮）
        List<ChatMessage> history = getRecentHistory(sessionId, 3);
        
        // 4. 构建完整消息列表
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(SystemMessage.from(SYSTEM_PROMPT));
        messages.addAll(history);
        messages.add(UserMessage.from(cleanQuestion));
        
        // 5. 调用AI
        String answer;
        int tokensUsed = 0;
        try {
            log.info("开始调用AI模型: messages size={}", messages.size());
            Response<AiMessage> response = chatModel.generate(messages);
            answer = response.content().text();
            tokensUsed = response.tokenUsage() != null 
                ? response.tokenUsage().totalTokenCount() 
                : 0;
            log.info("AI对话响应成功: tokens={}", tokensUsed);
        } catch (Exception e) {
            log.error("AI对话调用失败，详细错误信息: {}", e.getMessage(), e);
            throw new BusinessException(ErrorCode.AI_ERROR, "AI服务暂时不可用: " + e.getMessage());
        }
        
        // 6. 答案安全检查和过滤
        answer = contentFilter.filterResponse(answer);
        
        // 7. 保存对话记录
        saveConversation(userId, sessionId, cleanQuestion, answer, tokensUsed);
        
        // 8. 返回结果
        int remaining = rateLimiter.getRemainingCount(userId);
        log.info("AI对话完成: remaining={}", remaining);
        
        return new AIChatResponse(answer, sessionId, remaining);
    }
    
    @Override
    public List<ConversationVO> getHistory(String sessionId, Long userId) {
        log.info("查询对话历史: sessionId={}, userId={}", sessionId, userId);
        
        List<AIConversation> conversations = conversationMapper.selectList(
            new LambdaQueryWrapper<AIConversation>()
                .eq(AIConversation::getSessionId, sessionId)
                .eq(userId != null, AIConversation::getUserId, userId)
                .orderByAsc(AIConversation::getCreateTime)
                .last("LIMIT 50") // 最多返回50条
        );
        
        return conversations.stream()
            .map(this::toVO)
            .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void clearSession(Long userId, String sessionId) {
        log.info("清空会话: userId={}, sessionId={}", userId, sessionId);
        
        conversationMapper.delete(
            new LambdaQueryWrapper<AIConversation>()
                .eq(AIConversation::getSessionId, sessionId)
                .eq(Objects.nonNull(userId), AIConversation::getUserId, userId)
        );
    }
    
    /**
     * 获取最近N轮对话历史
     */
    private List<ChatMessage> getRecentHistory(String sessionId, int rounds) {
        // 每轮包括一问一答，所以查询 rounds * 2 条记录
        List<AIConversation> records = conversationMapper.selectList(
            new LambdaQueryWrapper<AIConversation>()
                .eq(AIConversation::getSessionId, sessionId)
                .orderByDesc(AIConversation::getCreateTime)
                .last("LIMIT " + (rounds * 2))
        );
        
        // 需要反转顺序（从旧到新）
        List<ChatMessage> messages = new ArrayList<>();
        for (int i = records.size() - 1; i >= 0; i--) {
            AIConversation record = records.get(i);
            messages.add(UserMessage.from(record.getQuestion()));
            messages.add(AiMessage.from(record.getAnswer()));
        }
        
        log.debug("加载对话历史: sessionId={}, 历史轮数={}", sessionId, messages.size() / 2);
        return messages;
    }
    
    /**
     * 保存对话记录
     */
    private void saveConversation(Long userId, String sessionId, 
                                   String question, String answer, int tokensUsed) {
        AIConversation conversation = new AIConversation();
        conversation.setUserId(userId);
        conversation.setSessionId(sessionId);
        conversation.setQuestion(question);
        conversation.setAnswer(answer);
        conversation.setTokensUsed(tokensUsed);
        conversation.setCreateTime(LocalDateTime.now());
        
        conversationMapper.insert(conversation);
        log.debug("保存对话记录: id={}", conversation.getId());
    }
    
    /**
     * Entity转VO
     */
    private ConversationVO toVO(AIConversation conversation) {
        return new ConversationVO(
            conversation.getId(),
            conversation.getQuestion(),
            conversation.getAnswer(),
            conversation.getCreateTime()
        );
    }
}
