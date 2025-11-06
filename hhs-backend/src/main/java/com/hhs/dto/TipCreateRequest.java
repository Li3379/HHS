package com.hhs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record TipCreateRequest(
        @NotBlank(message = "标题不能为空")
        @Size(max = 128, message = "标题长度不能超过128字符")
        String title,

        @NotBlank(message = "分类不能为空")
        String category,

        @Size(max = 10, message = "标签数量不可超过10个")
        List<@Size(max = 32, message = "单个标签长度不能超过32字符") String> tags,

        @NotBlank(message = "内容不能为空")
        String content,

        @Size(max = 512, message = "摘要长度不能超过512字符")
        String summary
) {
}
