package com.example.shop.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

/**
 * Docker profile 配置测试。
 */
class DockerProfileConfigTest {

    @Test
    void should_defineDockerDependencies_when_dockerProfileExists() throws IOException {
        String content = new ClassPathResource("application-docker.yml")
                .getContentAsString(StandardCharsets.UTF_8);

        assertThat(content).contains("MYSQL_HOST");
        assertThat(content).contains("REDIS_HOST");
        assertThat(content).contains("RABBITMQ_HOST");
        assertThat(content).contains("SHOP_AUTH_TOKEN_SECRET");
        assertThat(content).contains("/v3/api-docs");
        assertThat(content).contains("/swagger-ui.html");
    }
}
