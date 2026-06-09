package com.example.shop.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.shop.exception.BusinessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * common 和 exception 支撑类测试。
 */
class CommonSupportTest {

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void should_throwException_when_contextEmpty() {
        assertThatThrownBy(UserContext::getCurrentUserId)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("未登录");
    }

    @Test
    void should_returnConfiguredUserId_when_contextSet() {
        UserContext.setCurrentUserId(9L);

        assertThat(UserContext.getCurrentUserId()).isEqualTo(9L);
    }

    @Test
    void should_createApiResponse_when_factoryCalled() {
        ApiResponse<String> success = ApiResponse.success("ok");
        ApiResponse<String> namedSuccess = ApiResponse.success("done", "ok");
        ApiResponse<Void> error = ApiResponse.error(400, "bad");

        assertThat(success.getData()).isEqualTo("ok");
        assertThat(namedSuccess.getMessage()).isEqualTo("done");
        assertThat(error.getCode()).isEqualTo(400);
    }

    @Test
    void should_createBusinessException_when_messageOnly() {
        BusinessException exception = new BusinessException("失败");

        assertThat(exception.getCode()).isEqualTo(400);
        assertThat(exception.getMessage()).isEqualTo("失败");
    }
}
