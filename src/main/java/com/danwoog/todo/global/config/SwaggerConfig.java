package com.danwoog.todo.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Danwoog Todo API")
                        .description("단웅 Todo 프로젝트 API 문서입니다.")
                        .version("v1.0.0"));
    }
}