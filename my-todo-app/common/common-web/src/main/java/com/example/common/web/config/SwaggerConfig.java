package com.example.common.web.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI（Swagger）接口文档配置
 * <p>
 * 配置 API 文档的基本信息，包括标题、描述、版本号、联系方式和许可证。
 * 各微服务引入 common-web 模块后自动拥有 API 文档能力。
 * </p>
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("My Todo App API")
                        .description("SaaS Multi-tenant Enterprise Management System API")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Example Team")
                                .email("support@example.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }
}
