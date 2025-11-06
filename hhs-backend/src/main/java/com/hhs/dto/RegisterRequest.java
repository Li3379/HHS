package com.hhs.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "用户名不能为空")
        @Size(min = 4, max = 32, message = "用户名长度需在4-32之间")
        String username,

        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 64, message = "密码长度需在6-64之间")
        String password,

        @NotBlank(message = "昵称不能为空")
        @Size(max = 64, message = "昵称长度不能超过64字符")
        String nickname,

        @Email(message = "邮箱格式不正确")
        String email
) {
}
