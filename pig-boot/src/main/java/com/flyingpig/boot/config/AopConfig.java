package com.flyingpig.boot.config;

import org.springframework.aop.aspectj.annotation.AnnotationAwareAspectJAutoProxyCreator;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.core.Ordered;

@Configuration(proxyBeanMethods = false)  // 添加proxyBeanMethods = false
@Role(BeanDefinition.ROLE_INFRASTRUCTURE) // 标记为基础设施
public class AopConfig {

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)  // 标记Bean为基础设施
    public DefaultAdvisorAutoProxyCreator advisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator creator = new DefaultAdvisorAutoProxyCreator();
        creator.setProxyTargetClass(true);
        creator.setOrder(Ordered.HIGHEST_PRECEDENCE);  // 设置最高优先级
        return creator;
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)  // 标记Bean为基础设施
    public AnnotationAwareAspectJAutoProxyCreator aspectJAutoProxyCreator() {
        AnnotationAwareAspectJAutoProxyCreator creator = new AnnotationAwareAspectJAutoProxyCreator();
        creator.setOrder(Ordered.HIGHEST_PRECEDENCE);  // 设置最高优先级
        return creator;
    }
}