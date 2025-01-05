package com.flyingpig.ioc.registry;

import com.flyingpig.ioc.config.BeanPostProcessor;

import java.util.List;

/**
 * Bean后处理器注册表接口 -- 用于在注册后实例化前修改Bean的注册
 */
public interface BeanPostProcessorRegistry {

    /**
     * 向容器中添加一个 Bean 后处理器
     * Bean 后处理器可以在 Bean 实例化和初始化之前或之后对 Bean 进行修改
     *
     * @param processor 要添加的 Bean 后处理器
     */
    void addBeanPostProcessor(BeanPostProcessor processor);

    /**
     * 获取容器中所有已注册的 Bean 后处理器
     * 这个方法返回一个包含所有已注册后处理器的列表
     *
     * @return 已注册的 Bean 后处理器列表
     */
    List<BeanPostProcessor> getBeanPostProcessors();

    /**
     * 清除容器中的所有 Bean 后处理器
     * 通常在容器销毁或重新初始化时使用，清空所有已注册的后处理器
     */
    void clear();
}
