package com.hhs.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI hhsOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("HHS 健康生活小技巧平�?API")
                        .description("Health Hack System 后端接口文档")
                        .version("1.0.0")
                        .contact(new Contact().name("HHS Team")))
                .externalDocs(new ExternalDocumentation()
                        .description("项目仓库")
                        .url("https://example.com/hhs"));
    }
}
