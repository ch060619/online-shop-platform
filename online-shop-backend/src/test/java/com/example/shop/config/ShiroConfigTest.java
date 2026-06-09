package com.example.shop.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.shiro.realm.Realm;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.junit.jupiter.api.Test;

/**
 * ShiroConfig 单元测试。
 */
class ShiroConfigTest {

    @Test
    void should_createSecurityBeans_when_configCalled() {
        ShiroConfig config = new ShiroConfig();
        Realm realm = config.realm();
        DefaultWebSecurityManager securityManager = config.securityManager(realm);
        ShiroFilterFactoryBean filterFactoryBean = config.shiroFilterFactoryBean(securityManager);

        assertThat(realm).isNotNull();
        assertThat(securityManager).isNotNull();
        assertThat(filterFactoryBean).isNotNull();
    }
}
