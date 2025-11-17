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
@Tag(name = "AI功能")
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
    @Operation(summary = "AI智能分类内容")
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
    @Operation(summary = "AI健康顾问对话")
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
    @Operation(summary = "获取对话历史")
    @GetMapping("/chat/history")
    public Result<AIHistoryResponse> getHistory(@RequestParam String sessionId) {
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
    @Operation(summary = "清空对话历史")
    @DeleteMapping("/chat/history/{sessionId}")
    public Result<Void> clearHistory(@PathVariable String sessionId) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("清空对话历史: userId={}, sessionId={}", userId, sessionId);
        
        chatService.clearSession(userId, sessionId);
        return Result.success();
    }
}
























