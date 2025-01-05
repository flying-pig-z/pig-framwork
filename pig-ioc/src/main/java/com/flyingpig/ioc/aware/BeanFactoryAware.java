package com.flyingpig.ioc.aware;

import com.flyingpig.ioc.core.SimpleIoC;

public interface BeanFactoryAware {
    void setBeanFactory(SimpleIoC beanFactory);
}
