package com.flyingpig.boot;
import com.flyingpig.boot.annotation.SpringBootApplication;
import com.flyingpig.boot.config.ServerProperties;
import com.flyingpig.boot.server.*;
import com.flyingpig.mvc.core.DispatcherServlet;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.ResourcePropertySource;

public class SpringApplication {
    public static ApplicationContext run(Class<?> primarySource, String... args) {
        return new SpringApplication().doRun(primarySource, args);
    }

    private ApplicationContext doRun(Class<?> primarySource, String... args) {
        try {
            // 检查主类注解
            SpringBootApplication annotation = AnnotationUtils.findAnnotation(
                    primarySource, SpringBootApplication.class);
            if (annotation == null) {
                throw new IllegalStateException(
                        "The main class must be annotated with @SpringBootApplication");
            }

            // 创建Spring上下文
            AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

            // 加载配置文件，添加PropertySourcesPlaceholderConfigurer，使得配置文件能被解析
            PropertySource<?> propertySource = new ResourcePropertySource("application.properties");
            context.getEnvironment().getPropertySources().addFirst(propertySource);
            context.registerBean("propertySourcesPlaceholderConfigurer",
                    PropertySourcesPlaceholderConfigurer.class,
                    PropertySourcesPlaceholderConfigurer::new);

            // 刷新上下文，确保配置属性被加载
            context.refresh();

            // 注册主配置类和服务器配置类
            context.register(primarySource);
            context.register(ServerProperties.class);

            // 获取 ServerProperties，并创建 DispatcherServlet
            // 然后使用ServerProperties和DispatcherServlet通过工厂类创建 WebServer
            WebServer webServer = WebServerFactory.getInstance()
                    .createWebServer(context.getBean(ServerProperties.class), new DispatcherServlet(context));


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