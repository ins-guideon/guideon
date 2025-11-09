package com.guideon.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI 설정 클래스
 * 
 * Swagger UI 접속 경로: http://localhost:8080/swagger-ui.html
 * API 문서 JSON: http://localhost:8080/v3/api-docs
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Guideon API Documentation")
                        .version("1.0.0")
                        .description("AI 기반 규정 검색 시스템 API 문서\n\n" +
                                "이 API는 LangChain4j와 Google Gemini를 사용하여 규정 문서를 검색하고 질문에 답변하는 기능을 제공합니다.")
                        .contact(new Contact()
                                .name("Guideon Team")
                                .email("support@guideon.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("로컬 개발 서버"),
                        new Server().url("https://api.guideon.com").description("프로덕션 서버")
                ))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", 
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT 토큰을 입력하세요. 로그인 API에서 받은 accessToken을 사용합니다.")));
    }
}

