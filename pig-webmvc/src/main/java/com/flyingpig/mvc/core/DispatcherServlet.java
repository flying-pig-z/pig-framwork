package com.flyingpig.mvc.core;

import com.flyingpig.mvc.annotation.Controller;
import com.flyingpig.mvc.annotation.RequestMapping;
import com.flyingpig.mvc.annotation.RequestParam;
import com.flyingpig.mvc.annotation.ResponseBody;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class DispatcherServlet extends HttpServlet {
    private final Map<String, Method> handlerMapping = new ConcurrentHashMap<>(); // URL 到方法的映射
    private final Map<Method, Object> controllerMapping = new ConcurrentHashMap<>(); // 方法到控制器的映射
    private final Class<?> primarySource;

    private ApplicationContext applicationContext;

    /**
     * 设置 ApplicationContext。
     * @param applicationContext Spring 容器上下文
     */
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }



    /**
     * DispatcherServlet 的构造函数。
     * @param primarySource 主启动类
     */
    public DispatcherServlet(Class<?> primarySource) {
        this.primarySource = primarySource;
    }

    /**
     * 初始化 DispatcherServlet，设置 Handler 映射。
     * @throws ServletException 如果 applicationContext 未设置则抛出异常
     */
    @Override
    public void init() throws ServletException {
        if (applicationContext == null) {
            throw new ServletException("ApplicationContext must be set before initialization");
        }
        initHandlerMappings(primarySource.getPackage().getName());
    }

    /**
     * 初始化 Handler 映射。
     * @param basePackage 要扫描的包名
     */
    private void initHandlerMappings(String basePackage) {
        try {
            // 1. 从 Spring 容器获取 Controller
            Map<String, Object> controllers = applicationContext.getBeansWithAnnotation(Controller.class);
            if (controllers.isEmpty()) {
                // 2. 如果没有找到 Controller，则手动扫描包
                Set<Class<?>> classes = scanPackageClasses(basePackage);
                for (Class<?> clazz : classes) {
                    if (clazz.isAnnotationPresent(Controller.class)) {
                        // 从 Spring 容器获取实例，如果没有则注册
                        Object controller;
                        try {
                            controller = applicationContext.getBean(clazz);
                        } catch (NoSuchBeanDefinitionException e) {
                            // 如果 Spring 容器中没有，则创建并注册
                            controller = clazz.getDeclaredConstructor().newInstance();
                            ((ConfigurableApplicationContext) applicationContext)
                                    .getBeanFactory()
                                    .registerSingleton(
                                            clazz.getSimpleName(),
                                            controller
                                    );
                        }

                        registerController(clazz, controller);
                    }
                }
            } else {
                // 处理已找到的 Controller
                controllers.forEach((name, controller) ->
                        registerController(controller.getClass(), controller));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to init handler mappings", e);
        }
    }

    /**
     * 注册控制器及其方法。
     * @param controllerClass 控制器类
     * @param controller 控制器实例
     */
    private void registerController(Class<?> controllerClass, Object controller) {
        // 处理类级别的 RequestMapping
        String baseUrl = "";
        if (controllerClass.isAnnotationPresent(RequestMapping.class)) {
            baseUrl = controllerClass.getAnnotation(RequestMapping.class).value();
        }

        // 处理方法级别的 RequestMapping
        for (Method method : controllerClass.getMethods()) {
            if (method.isAnnotationPresent(RequestMapping.class)) {
                String methodUrl = method.getAnnotation(RequestMapping.class).value();
                String fullUrl = combineUrl(baseUrl, methodUrl);
                handlerMapping.put(fullUrl, method);
                controllerMapping.put(method, controller);
                System.out.println("Mapped URL: " + fullUrl + " to " + method);
            }
        }
    }

    /**
     * 扫描包并获取所有类。
     * @param basePackage 要扫描的包名
     * @return 包中的类集合
     */
    private Set<Class<?>> scanPackageClasses(String basePackage) {
        Set<Class<?>> classes = new HashSet<>();
        try {
            String path = basePackage.replace('.', '/');
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> resources = classLoader.getResources(path);

            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                if (resource.getProtocol().equals("file")) {
                    File directory = new File(resource.toURI());
                    scanDirectory(directory, basePackage, classes);
                } else if (resource.getProtocol().equals("jar")) {
                    scanJarFile(resource, path, classes);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to scan package: " + basePackage, e);
        }
        return classes;
    }

    /**
     * 扫描文件目录以找到类。
     * @param directory 文件目录
     * @param packageName 包名
     * @param classes 类集合
     */
    private void scanDirectory(File directory, String packageName, Set<Class<?>> classes) {
        if (!directory.exists()) {
            return;
        }

        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    scanDirectory(file, packageName + "." + file.getName(), classes);
                } else if (file.getName().endsWith(".class")) {
                    String className = packageName + '.' +
                            file.getName().substring(0, file.getName().length() - 6);
                    try {
                        classes.add(Class.forName(className));
                    } catch (ClassNotFoundException e) {
                        // 忽略无法加载的类
                    }
                }
            }
        }
    }

    /**
     * 扫描 JAR 文件以找到类。
     * @param url JAR 文件 URL
     * @param path 包路径
     * @param classes 类集合
     */
    private void scanJarFile(URL url, String path, Set<Class<?>> classes) {
        try {
            String jarPath = url.getPath().substring(5, url.getPath().indexOf("!"));
            JarFile jar = new JarFile(URLDecoder.decode(jarPath, StandardCharsets.UTF_8));
            Enumeration<JarEntry> entries = jar.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.startsWith(path) && name.endsWith(".class")) {
                    String className = name.substring(0, name.length() - 6).replace('/', '.');
                    try {
                        classes.add(Class.forName(className));
                    } catch (ClassNotFoundException e) {
                        // 忽略无法加载的类
                    }
                }
            }
            jar.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed to scan JAR file", e);
        }
    }

    /**
     * 合并类级别和方法级别的 URL。
     * @param baseUrl 类级别的 URL
     * @param methodUrl 方法级别的 URL
     * @return 合并后的 URL
     */
    private String combineUrl(String baseUrl, String methodUrl) {
        // 移除开头和结尾的斜杠，然后重新添加
        baseUrl = baseUrl.trim().replaceAll("^/+|/+$", "");
        methodUrl = methodUrl.trim().replaceAll("^/+|/+$", "");
        return "/" + baseUrl + (baseUrl.isEmpty() ? "" : "/") + methodUrl;
    }

    /**
     * 处理 GET 请求。
     * @param req HTTP 请求
     * @param resp HTTP 响应
     * @throws ServletException Servlet 异常
     * @throws IOException IO 异常
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 获取请求路径
        String uri = req.getRequestURI();
        Method handlerMethod = handlerMapping.get(uri);

        if (handlerMethod != null) {
            Object controller = controllerMapping.get(handlerMethod);
            try {
                // 获取方法参数
                Parameter[] parameters = handlerMethod.getParameters();
                Object[] args = new Object[parameters.length];

                // 处理参数注入
                for (int i = 0; i < parameters.length; i++) {
                    Parameter parameter = parameters[i];
                    if (parameter.getType().equals(HttpServletRequest.class)) {
                        args[i] = req;
                    } else if (parameter.getType().equals(HttpServletResponse.class)) {
                        args[i] = resp;
                    } else if (parameter.isAnnotationPresent(RequestParam.class)) {
                        String paramName = parameter.getAnnotation(RequestParam.class).value();
                        args[i] = req.getParameter(paramName);
                    } else {
                        // 尝试从 Spring 上下文中获取 Bean
                        try {
                            args[i] = applicationContext.getBean(parameter.getType());
                        } catch (Exception e) {
                            args[i] = null;
                        }
                    }
                }

                // 执行方法
                Object result = handlerMethod.invoke(controller, args);

                // 处理返回结果
                if (handlerMethod.isAnnotationPresent(ResponseBody.class)) {
                    resp.setContentType("application/json");
                    resp.getWriter().write(result != null ? result.toString() : "");
                } else {
                    resp.getWriter().write(result != null ? result.toString() : "");
                }
            } catch (Exception e) {
                e.printStackTrace();
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("Internal Server Error: " + e.getMessage());
            }
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("Not Found");
        }
    }
}
