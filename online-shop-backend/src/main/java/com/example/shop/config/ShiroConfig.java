package com.example.shop.config;

import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.realm.text.IniRealm;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Apache Shiro 基础配置。
 */
@Configuration
public class ShiroConfig {

    /**
     * 创建实验版 Realm，后续可替换为数据库认证 Realm。
     *
     * @return Shiro Realm
     */
    @Bean
    public Realm realm() {
        return new IniRealm("classpath:shiro.ini");
    }

    /**
     * 创建安全管理器。
     *
     * @param realm Shiro Realm
     * @return Shiro Web 安全管理器
     */
    @Bean
    public DefaultWebSecurityManager securityManager(Realm realm) {
        return new DefaultWebSecurityManager(realm);
    }

    /**
     * 创建 Shiro 过滤器工厂。
     *
     * @param securityManager Shiro 安全管理器
     * @return Shiro 过滤器工厂
     */
    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean(DefaultWebSecurityManager securityManager) {
        ShiroFilterFactoryBean bean = new ShiroFilterFactoryBean();
        bean.setSecurityManager(securityManager);
        Map<String, String> filterChain = new LinkedHashMap<>();
        filterChain.put("/api/**", "anon");
        bean.setFilterChainDefinitionMap(filterChain);
        return bean;
    }
}
