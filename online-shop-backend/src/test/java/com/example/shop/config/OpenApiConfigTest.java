package com.example.shop.config;

import static org.assertj.core.api.Assertions.assertThat;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;

/**
 * OpenApiConfig 测试。
 */
class OpenApiConfigTest {

    @Test
    void should_createOpenApiMetadata_when_configLoaded() {
        OpenAPI openAPI = new OpenApiConfig().onlineShopOpenApi();

        assertThat(openAPI.getInfo().getTitle()).isEqualTo("Online Shop Platform API");
        assertThat(openAPI.getInfo().getVersion()).isEqualTo("2.2.0");
        assertThat(openAPI.getComponents().getSecuritySchemes()).containsKey("bearerAuth");
        assertThat(openAPI.getSecurity()).hasSize(1);
    }
}
