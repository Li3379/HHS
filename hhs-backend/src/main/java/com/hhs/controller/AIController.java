package com.hhs.controller;

import com.hhs.common.Result;
import com.hhs.dto.ai.AIChatRequest;
import com.hhs.dto.ai.AIChatResponse;
import com.hhs.dto.ai.AIClassifyRequest;
import com.hhs.dto.ai.AIClassifyResponse;
import com.hhs.dto.ai.AIHistoryResponse;
import com.hhs.dto.ai.ConversationVO;
import com.hhs.component.AIRateLimiter;
import com.hhs.security.SecurityUtils;
import com.hhs.service.AIChatService;
import com.hhs.service.AIClassifyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AI功能Controller
 */
@Slf4j
@Tag(name = "AI功能模块", description = "AI智能分类、AI健康顾问对话功能")
@RestController
@RequestMapping("/api/ai")
public class AIController {
    
    @Autowired
    private AIClassifyService classifyService;
    
    @Autowired
    private AIChatService chatService;
    
    @Autowired
    private AIRateLimiter rateLimiter;
    
    /**
     * AI智能分类
     */
    @Operation(
        summary = "AI智能分类",
        description = "根据标题和内容自动分类（饮食/运动/睡眠/心理）并推荐标签"
    )
    @PostMapping("/classify")
    @PreAuthorize("isAuthenticated()")
    public Result<AIClassifyResponse> classify(@RequestBody @Valid AIClassifyRequest request) {
        log.info("AI分类请求: title={}", request.title());
        
        AIClassifyResponse response = classifyService.classify(
            request.title(), 
            request.content()
        );
        
        return Result.success(response);
    }
    
    /**
     * AI对话
     */
    @Operation(
        summary = "AI健康顾问对话",
        description = "与AI健康顾问进行对话，获取健康建议（每天限频10次）"
    )
    @PostMapping("/chat")
    public Result<AIChatResponse> chat(@RequestBody @Valid AIChatRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("AI对话请求: userId={}, sessionId={}", userId, request.sessionId());
        
        AIChatResponse response = chatService.chat(
            userId,
            request.sessionId(),
            request.question()
        );
        
        return Result.success(response);
    }
    
    /**
     * 获取对话历史
     */
    @Operation(
        summary = "获取对话历史",
        description = "查询指定会话的历史消息记录"
    )
    @GetMapping("/chat/history")
    public Result<AIHistoryResponse> getHistory(
            @Parameter(description = "会话ID", required = true, example = "session_123") 
            @RequestParam String sessionId) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("查询对话历史: userId={}, sessionId={}", userId, sessionId);
        
        List<ConversationVO> history = chatService.getHistory(sessionId, userId);
        int remainingCount = rateLimiter.getRemainingCount(userId);
        
        AIHistoryResponse response = new AIHistoryResponse(history, remainingCount);
        return Result.success(response);
    }
    
    /**
     * 清空对话历史
     */
    @Operation(
        summary = "清空对话历史",
        description = "删除指定会话的所有历史消息"
    )
    @DeleteMapping("/chat/history/{sessionId}")
    public Result<Void> clearHistory(
            @Parameter(description = "会话ID", required = true, example = "session_123") 
            @PathVariable String sessionId) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("清空对话历史: userId={}, sessionId={}", userId, sessionId);
        
        chatService.clearSession(userId, sessionId);
        return Result.success();
    }
}



























