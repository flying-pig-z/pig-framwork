package com.flyingpig.boot.autoconfigure;


import com.flyingpig.boot.annotation.EnableAutoConfiguration;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

public class AutoConfigurationImportSelector implements ImportSelector {

    // Spring Boot 自动配置相关的资源文件路径常量
    private static final String FACTORIES_RESOURCE_LOCATION = "META-INF/spring.factories";

    // SpringBoot3.x的配置文件路径，这里没有适配，还是SpringBoot2.x的模式
    private static final String AUTOCONFIGURE_IMPORTS_LOCATION =
            "META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports";


    /**
     * 选择并返回要导入的配置类的名称。
     * 这个方法是 ImportSelector 接口的实现方法，Spring Boot 使用它来选择自动配置类。
     *
     * @param annotationMetadata 注解元数据
     * @return 需要导入的配置类的全限定类名数组
     */
    @Override
    public String[] selectImports(AnnotationMetadata annotationMetadata) {
        List<String> configurations = new ArrayList<>();

        // 1. 从 spring.factories 加载自动配置（SpringBoot2.x）
        configurations.addAll(loadSpringFactories());

        // 2. 从 AutoConfiguration.imports 加载配置（SpringBoot3.x）
        configurations.addAll(loadAutoConfigurationImports());

        // 3. 去重
        return StringUtils.toStringArray(filter(configurations));
    }

    /**
     * 确定要扫描的基础包
     */
    private String determineBasePackage(AnnotationMetadata metadata) {
        // 获取@SpringBootApplication注解所在的包
        String className = metadata.getClassName();
        return className.substring(0, className.lastIndexOf('.'));
    }

    /**
     * 从 spring.factories 文件中加载自动配置类。
     *
     * @return 自动配置类的全限定类名列表
     */
    private List<String> loadSpringFactories() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        List<String> autoConfigurations = new ArrayList<>();

        try {
            // 获取所有 spring.factories 文件的 URL
            Enumeration<URL> resources = classLoader.getResources(FACTORIES_RESOURCE_LOCATION);

            // 遍历每个 spring.factories 文件
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                Properties properties = new Properties();

                // 加载配置文件
                try (InputStream inputStream = url.openStream()) {
                    properties.load(inputStream);
                }

                // 获取 EnableAutoConfiguration 对应的配置类
                String factoryClassNames = properties.getProperty(
                        EnableAutoConfiguration.class.getName());

                if (factoryClassNames != null) {
                    // 分割并添加到结果列表中
                    for (String factoryClassName : factoryClassNames.split(",")) {
                        // 去除空白字符
                        String trimmedClassName = factoryClassName.trim();
                        if (!trimmedClassName.isEmpty()) {
                            autoConfigurations.add(trimmedClassName);
                        }
                    }
                }
            }

            // 去重
            return new ArrayList<>(new LinkedHashSet<>(autoConfigurations));

        } catch (IOException e) {
            throw new RuntimeException("Error loading spring.factories", e);
        }
    }

    /**
     * 从 AutoConfiguration.imports 文件中加载自动配置类。
     * 该文件位于 "META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports" 路径下。
     *
     * @return 自动配置类的全限定类名列表
     */
    private List<String> loadAutoConfigurationImports() {
        List<String> configurations = new ArrayList<>();
        try {
            // 获取所有文件的资源路径
            Enumeration<URL> urls = getClass().getClassLoader()
                    .getResources(AUTOCONFIGURE_IMPORTS_LOCATION);

            // 遍历每个资源文件
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(url.openStream()))) {
                    String line;
                    // 逐行读取文件内容
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        // 如果该行非空且不是注释，则添加到配置列表中
                        if (line.length() > 0 && !line.startsWith("#")) {
                            configurations.add(line);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println("未读取到配置文件");
            // 如果在加载过程中遇到错误（如文件不存在），捕获异常并继续执行
            // 在实际应用中可以通过日志记录这个错误
        }
        return configurations;
    }

    /**
     * 对加载的配置类进行去重操作。
     * 由于可能存在重复的配置类，这里使用 LinkedHashSet 来去除重复项，并保持原有的插入顺序。
     *
     * @param configurations 待去重的配置类列表
     * @return 去重后的配置类列表
     */
    private List<String> filter(List<String> configurations) {
        // 使用 LinkedHashSet 去重，保证了元素的唯一性，并且保留了插入顺序
        Set<String> uniqueConfigs = new LinkedHashSet<>(configurations);
        return new ArrayList<>(uniqueConfigs);
    }
}
