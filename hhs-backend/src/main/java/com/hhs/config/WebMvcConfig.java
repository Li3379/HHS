package com.hhs.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload.path:./uploads}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 规范化路径，确保使用绝对路径
        String normalizedPath = Paths.get(uploadPath).toAbsolutePath().normalize().toString();
        // 统一使用正斜杠（Spring ResourceHandler 需要）
        normalizedPath = normalizedPath.replace("\\", "/");
        // 确保路径以斜杠结尾
        if (!normalizedPath.endsWith("/")) {
            normalizedPath += "/";
        }
        
        // 配置上传文件的访问路径
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + normalizedPath)
                .resourceChain(false); // 禁用资源链，避免缓存问题
    }
}

