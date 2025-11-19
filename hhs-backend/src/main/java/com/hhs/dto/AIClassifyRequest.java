package com.hhs.dto;

import jakarta.validation.constraints.NotBlank;

public record AIClassifyRequest(
        @NotBlank(message = "文本内容不能为空")
        String text
) {
}



























