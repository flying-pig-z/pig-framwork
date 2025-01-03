package com.flyingpig.boot;
import com.flyingpig.boot.annotation.EnableAutoConfiguration;
import com.flyingpig.boot.annotation.SpringBootApplication;
import com.flyingpig.boot.autoconfigure.AutoConfigurationImportSelector;
import com.flyingpig.boot.config.ServerProperties;
import com.flyingpig.boot.server.*;
import com.flyingpig.mvc.core.DispatcherServlet;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.ResourcePropertySource;
import org.springframework.core.type.AnnotationMetadata;

public class SpringApplication {
    public static ApplicationContext run(Class<?> primarySource, String... args) {
        return new SpringApplication().doRun(primarySource, args);
    }

    private ApplicationContext doRun(Class<?> primarySource, String... args) {
        try {
            // 1.先处理主类的注解
            SpringBootApplication annotation = AnnotationUtils.findAnnotation(
                    primarySource, SpringBootApplication.class);
            if (annotation == null) {
                throw new IllegalStateException(
                        "The main class must be annotated with @SpringBootApplication");
            }

            AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

            // 2.加载配置文件
            PropertySource<?> propertySource = new ResourcePropertySource("application.properties");
            context.getEnvironment().getPropertySources().addFirst(propertySource);
            context.registerBean("propertySourcesPlaceholderConfigurer",
                    PropertySourcesPlaceholderConfigurer.class,
                    PropertySourcesPlaceholderConfigurer::new);



            // 3.扫描包注册Bean
            // 包括主类所在的包和其他第三方依赖的自动配置类
            // @SpringBootApplication 注解中包含了 @EnableAutoConfiguration
            // Spring 的注解处理器在 scan 时会处理这些注解
            // 当处理到 @EnableAutoConfiguration 时，会自动调用 AutoConfigurationImportSelector
            context.scan(primarySource.getPackage().getName());

            // 4.刷新上下文
            // 如果不调用refresh()，Bean只是被注册了定义，但没有被实例化
            context.refresh();


            context.register(ServerProperties.class);

            // 创建并配置Web服务器
            WebServer webServer = WebServerFactory.getInstance()
                    .createWebServer(context.getBean(ServerProperties.class),
                            new DispatcherServlet(context));

            // 启动Web服务器
            webServer.start();

            // 添加关闭钩子
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    webServer.stop();
                    context.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }));

            // 等待服务器运行
            Thread.currentThread().join();

            return context;
        } catch (Exception e) {
            throw new RuntimeException("Failed to start application", e);
        }
    }
}