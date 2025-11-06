package com.hhs.controller;

import com.hhs.common.Result;
import com.hhs.dto.AIClassifyRequest;
import com.hhs.vo.AIClassifyResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    @PostMapping("/classify")
    public Result<AIClassifyResponse> classify(@RequestBody @Valid AIClassifyRequest request) {
        // TODO: 集成真实的 AI 分类服务（LangChain4j + HanLP）
        // 目前返回 Mock 数据，避免前端报错
        
        String text = request.text().toLowerCase();
        String category = "diet"; // 默认分类
        List<String> tags = Arrays.asList("健康", "生活");
        
        // 简单的关键词匹配
        if (text.contains("运动") || text.contains("锻炼") || text.contains("跑步") || text.contains("健身")) {
            category = "fitness";
            tags = Arrays.asList("运动", "健身", "锻炼");
        } else if (text.contains("睡眠") || text.contains("休息") || text.contains("失眠")) {
            category = "sleep";
            tags = Arrays.asList("睡眠", "休息", "作息");
        } else if (text.contains("心理") || text.contains("情绪") || text.contains("压力") || text.contains("焦虑")) {
            category = "mental";
            tags = Arrays.asList("心理", "情绪", "减压");
        } else if (text.contains("饮食") || text.contains("营养") || text.contains("食物") || text.contains("吃")) {
            category = "diet";
            tags = Arrays.asList("饮食", "营养", "健康");
        }
        
        AIClassifyResponse response = new AIClassifyResponse(category, tags);
        return Result.success(response);
    }
}




