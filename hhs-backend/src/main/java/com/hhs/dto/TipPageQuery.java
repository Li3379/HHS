package com.hhs.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record TipPageQuery(
        @Min(value = 1, message = "页码最小为1")
        int page,

        @Min(value = 1, message = "分页大小最小为1")
        @Max(value = 50, message = "分页大小最大为50")
        int size,

        String category,

        String keyword
) {
    public TipPageQuery {
        page = Math.max(page, 1);
        size = Math.max(1, Math.min(size, 50));
    }
}
