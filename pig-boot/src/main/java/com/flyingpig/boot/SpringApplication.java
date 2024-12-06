package com.flyingpig.boot;
import com.flyingpig.boot.annotation.SpringBootApplication;
import com.flyingpig.boot.config.ServerProperties;
import com.flyingpig.boot.server.*;
import com.flyingpig.mvc.core.DispatcherServlet;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;

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
            AnnotationConfigApplicationContext context =
                    new AnnotationConfigApplicationContext();

            // 注册主配置类和服务器配置类
            context.register(primarySource);
            context.register(ServerProperties.class);

            // 设置包扫描
            context.scan(primarySource.getPackage().getName());

            // 创建并注册 DispatcherServlet
            DispatcherServlet dispatcherServlet = new DispatcherServlet(primarySource);
            dispatcherServlet.setApplicationContext(context);
            context.registerBean("dispatcherServlet", DispatcherServlet.class, () -> dispatcherServlet);

            // 刷新上下文，确保配置属性被加载
            context.refresh();
            // 从Spring上下文获取ServerProperties
            ServerProperties serverProperties = context.getBean(ServerProperties.class);



            // 使用工厂创建WebServer
            WebServer webServer = WebServerFactory.getInstance()
                    .createWebServer(serverProperties, dispatcherServlet);

            // 注册WebServer
            context.registerBean("webServer", WebServer.class, () -> webServer);

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