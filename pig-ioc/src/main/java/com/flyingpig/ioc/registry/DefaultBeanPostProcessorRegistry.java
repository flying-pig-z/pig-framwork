package com.flyingpig.ioc.registry;

import com.flyingpig.ioc.config.BeanPostProcessor;

import java.util.ArrayList;
import java.util.List;

// 默认Bean后处理器注册表实现
public class DefaultBeanPostProcessorRegistry implements BeanPostProcessorRegistry {
    private final List<BeanPostProcessor> processors = new ArrayList<>();

    @Override
    public void addBeanPostProcessor(BeanPostProcessor processor) {
        processors.add(processor);
    }

    @Override
    public List<BeanPostProcessor> getBeanPostProcessors() {
        return new ArrayList<>(processors);
    }

    @Override
    public void clear() {
        processors.clear();
    }
}
