package com.hhs.service;

import com.hhs.dto.ai.AIClassifyResponse;

/**
 * AI内容分类服务
 */
public interface AIClassifyService {
    
    /**
     * 智能分类健康技巧内容
     * @param title 标题
     * @param content 内容
     * @return 分类结果（category, tags, summary）
     */
    AIClassifyResponse classify(String title, String content);
}
