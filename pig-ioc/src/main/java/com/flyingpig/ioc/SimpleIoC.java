package com.flyingpig.ioc;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class SimpleIoC {
    // 存储Bean定义的容器
    private Map<String, BeanDefinition> beanDefinitions = new ConcurrentHashMap<>();

    // 添加获取BeanDefinition的方法，用于测试
    public BeanDefinition getBeanDefinition(String beanName) {
        return beanDefinitions.get(beanName);
    }

    // 单例Bean的缓存
    private Map<String, Object> singletonObjects = new ConcurrentHashMap<>();


    HashMap<String, Method> destroyMethods = new HashMap<>();

    // 注册带有@Component注解的类
    public void registerComponent(Class<?> clazz) {
        Component component = clazz.getAnnotation(Component.class);
        if (component != null) {
            String beanName = component.value().isEmpty() ?
                    toLowerFirstCase(clazz.getSimpleName()) : component.value();
            registerBeanDefinition(beanName, new BeanDefinition(clazz, beanName));
        }
    }

    // 注册带有@Bean注解的方法
    public void registerBean(String beanName, Class<?> beanClass) {
        registerBeanDefinition(beanName, new BeanDefinition(beanClass, beanName));
    }

    private void registerBeanDefinition(String beanName, BeanDefinition beanDefinition) {
        beanDefinitions.put(beanName, beanDefinition);
    }

    // 获取Bean实例
    public Object getBean(String beanName) throws Exception {
        BeanDefinition beanDefinition = beanDefinitions.get(beanName);
        if (beanDefinition == null) {
            throw new Exception("No bean named '" + beanName + "' is defined");
        }

        // 如果是单例模式且已经创建过，直接返回缓存的实例
        if (beanDefinition.getScope() == BeanScope.SINGLETON) {
            Object singletonObject = singletonObjects.get(beanName);
            if (singletonObject != null) {
                return singletonObject;
            }
            synchronized (this.singletonObjects) {
                singletonObject = singletonObjects.get(beanName);
                if (singletonObject == null) {
                    singletonObject = createBean(beanDefinition);
                    singletonObjects.put(beanName, singletonObject);
                }
                return singletonObject;
            }
        }

        // 如果是prototype模式，每次都创建新实例
        return createBean(beanDefinition);
    }

    // 创建Bean实例
    // 属性解析器
    private Object resolveValue(String value, Class<?> type) {
        if (type == Integer.class || type == int.class) {
            return Integer.parseInt(value);
        } else if (type == Long.class || type == long.class) {
            return Long.parseLong(value);
        } else if (type == Boolean.class || type == boolean.class) {
            return Boolean.parseBoolean(value);
        } else if (type == Double.class || type == double.class) {
            return Double.parseDouble(value);
        } else if (type == Float.class || type == float.class) {
            return Float.parseFloat(value);
        }
        return value;
    }

    private Object createBean(BeanDefinition beanDefinition) throws Exception {
        Class<?> beanClass = beanDefinition.getBeanClass();
        Object instance;

        // 先检查是否有带@Autowired的构造函数
        Constructor<?>[] constructors = beanClass.getDeclaredConstructors();
        Constructor<?> autowiredConstructor = null;

        for (Constructor<?> constructor : constructors) {
            if (constructor.isAnnotationPresent(Autowired.class)) {
                autowiredConstructor = constructor;
                break;
            }
        }

        // 如果找到带@Autowired的构造函数，使用该构造函数创建实例
        if (autowiredConstructor != null) {
            autowiredConstructor.setAccessible(true);
            Class<?>[] parameterTypes = autowiredConstructor.getParameterTypes();
            Object[] parameters = new Object[parameterTypes.length];

            for (int i = 0; i < parameterTypes.length; i++) {
                parameters[i] = getBean(toLowerFirstCase(parameterTypes[i].getSimpleName()));
            }

            instance = autowiredConstructor.newInstance(parameters);
        } else {
            // 如果没有带@Autowired的构造函数，尝试使用默认构造函数
            try {
                instance = beanClass.getDeclaredConstructor().newInstance();
            } catch (NoSuchMethodException e) {
                // 如果没有默认构造函数，使用第一个可用的构造函数
                Constructor<?> constructor = constructors[0];
                constructor.setAccessible(true);
                Class<?>[] parameterTypes = constructor.getParameterTypes();
                Object[] parameters = new Object[parameterTypes.length];

                for (int i = 0; i < parameterTypes.length; i++) {
                    parameters[i] = getBean(toLowerFirstCase(parameterTypes[i].getSimpleName()));
                }

                instance = constructor.newInstance(parameters);
            }
        }

        // 属性注入
        Field[] fields = beanClass.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Autowired.class)) {
                field.setAccessible(true);
                field.set(instance, getBean(toLowerFirstCase(field.getType().getSimpleName())));
            }
        }

        // Setter方法注入
        Method[] methods = beanClass.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(Autowired.class) &&
                    method.getName().startsWith("set") &&
                    method.getParameterCount() == 1) {

                Class<?> paramType = method.getParameterTypes()[0];
                method.invoke(instance, getBean(toLowerFirstCase(paramType.getSimpleName())));
            }
        }

        // 处理属性注入
        for (Field field : fields) {
            if (field.isAnnotationPresent(Value.class)) {
                field.setAccessible(true);
                Value valueAnnotation = field.getAnnotation(Value.class);
                Object value = resolveValue(valueAnnotation.value(), field.getType());
                field.set(instance, value);
            }
        }

        // 处理Aware接口
        String beanName = beanDefinition.getBeanName();
        if (instance instanceof BeanNameAware) {
            ((BeanNameAware) instance).setBeanName(beanName);
        }
        if (instance instanceof BeanFactoryAware) {
            ((BeanFactoryAware) instance).setBeanFactory(this);
        }

        // 调用初始化方法（@PostConstruct注解的方法）
        for (Method method : beanClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(PostConstruct.class)) {
                method.setAccessible(true);
                try {
                    method.invoke(instance);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to invoke @PostConstruct method", e);
                }
            }
        }

        // 注册销毁方法（@PreDestroy注解的方法）
        for (Method method : beanClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(PreDestroy.class)) {
                method.setAccessible(true);
                destroyMethods.put(beanName, method);
                break; // 只注册第一个销毁方法
            }
        }

        return instance;
    }

    public void close() {
        // 遍历destroyMethods并调用销毁方法
        for (Map.Entry<String, Method> entry : destroyMethods.entrySet()) {
            String beanName = entry.getKey();
            Method destroyMethod = entry.getValue();
            try {
                Object bean = singletonObjects.get(beanName);
                destroyMethod.invoke(bean);
            } catch (Exception e) {
                throw new RuntimeException("Failed to invoke @PreDestroy method", e);
            }
        }
    }

    // 工具方法：将类名首字母转小写
    private String toLowerFirstCase(String str) {
        char[] chars = str.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }
}
