package com.flyingpig.mvc.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flyingpig.mvc.annotation.RequestParam;
import com.flyingpig.mvc.annotation.ResponseBody;
import com.flyingpig.mvc.annotation.RestController;
import com.flyingpig.mvc.annotation.mapping.*;
import com.flyingpig.mvc.util.PackageScanner;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import com.flyingpig.mvc.annotation.Controller;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DispatcherServlet：请求分发处理器
 * 负责处理所有的 HTTP 请求的分发和处理
 */
public class DispatcherServlet extends HttpServlet {
    /**
     * JSON序列化/反序列化工具
     * 用于处理Controller返回对象与JSON字符串之间的转换
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 请求映射信息与处理方法的对应关系
     * key: 包含URL路径和HTTP方法的RequestMappingInfo对象
     * value: 对应的Controller方法
     */
    private final Map<RequestMappingInfo, Method> handlerMapping = new ConcurrentHashMap<>();

    /**
     * Controller方法与其所属Controller实例的对应关系
     * key: Controller中的方法对象
     * value: 方法所属的Controller实例
     */
    private final Map<Method, Object> controllerMapping = new ConcurrentHashMap<>();

    /**
     * 应用程序的主类，用于确定包扫描的起始位置，通常是@SpringBootApplication注解标注的类
     */
    private final Class<?> primarySource;

    /**
     * Spring应用上下文，用于获取Spring容器中的Bean实例，处理依赖注入等
     */
    private ApplicationContext applicationContext;


    /**
     * 构造函数
     *
     * @param primarySource 主应用类
     */
    public DispatcherServlet(Class<?> primarySource) {
        System.out.println("Initializing DispatcherServlet with primary source: " + primarySource.getName());
        this.primarySource = primarySource;
    }

    /**
     * 设置 ApplicationContext
     *
     * @param applicationContext Spring应用上下文
     */
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Servlet 初始化
     */
    @Override
    public void init() throws ServletException {
        if (applicationContext == null) {
            throw new ServletException("ApplicationContext must be set before initialization");
        }
        try {
            initHandlerMappings(primarySource.getPackage().getName());
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException("Failed to initialize DispatcherServlet", e);
        }
    }

    /**
     * 初始化处理器映射
     */
    private void initHandlerMappings(String basePackage) {
        try {
            // 从 Spring 容器获取 Controller
            Map<String, Object> controllers = applicationContext.getBeansWithAnnotation(Controller.class);
            if (!controllers.isEmpty()) {
                // 如果不为空，直接注册
                controllers.forEach((name, controller) -> {
                    registerController(controller.getClass(), controller);
                });
            } else {
                // 如果为空，扫描包中的类，如果类的上面含有Controller注解进行注册
                Set<Class<?>> classes = PackageScanner.scanPackageClasses(basePackage);
                for (Class<?> clazz : classes) {
                    if (clazz.isAnnotationPresent(Controller.class)) {
                        registerControllerClass(clazz);
                    }
                }
            }
            // 打印所有映射
            printMappings();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize handler mappings", e);
        }
    }

    /**
     * 注册控制器类
     */
    private void registerControllerClass(Class<?> clazz) {
        try {
            Object controller;
            try {
                controller = applicationContext.getBean(clazz);
            } catch (NoSuchBeanDefinitionException e) {
                controller = clazz.getDeclaredConstructor().newInstance();
                ((ConfigurableApplicationContext) applicationContext)
                        .getBeanFactory()
                        .registerSingleton(clazz.getSimpleName(), controller);
            }
            registerController(clazz, controller);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to register controller: " + clazz.getName(), e);
        }
    }

    /**
     * 注册控制器
     */
    private void registerController(Class<?> controllerClass, Object controller) {

        // 处理类级别的 RequestMapping
        String baseUrl = "";
        String baseMethod = "";
        if (controllerClass.isAnnotationPresent(RequestMapping.class)) {
            RequestMapping classMapping = controllerClass.getAnnotation(RequestMapping.class);
            baseUrl = classMapping.value();
            baseMethod = classMapping.method();
        }

        // 处理方法级别的 RequestMapping
        for (Method method : controllerClass.getMethods()) {
            if (method.isAnnotationPresent(RequestMapping.class) ||
                    method.isAnnotationPresent(GetMapping.class) ||
                    method.isAnnotationPresent(PostMapping.class) ||
                    method.isAnnotationPresent(PutMapping.class) ||
                    method.isAnnotationPresent(DeleteMapping.class)) {
                registerMethod(method, controller, baseUrl, baseMethod);
            }
        }
    }

    /**
     * 注册控制器方法
     * @param method 要注册的方法
     * @param controller 控制器实例
     * @param baseUrl 基础URL（来自类级别的RequestMapping）
     * @param baseMethod 基础HTTP方法（来自类级别的RequestMapping）
     */
    /**
     * 注册控制器方法
     *
     * @param method     要注册的方法
     * @param controller 控制器实例
     * @param baseUrl    基础URL（来自类级别的RequestMapping）
     * @param baseMethod 基础HTTP方法（来自类级别的RequestMapping）
     */
    private void registerMethod(Method method, Object controller, String baseUrl, String baseMethod) {
        String methodUrl = "";
        String httpMethod = baseMethod;

        // 获取方法的映射信息
        if (method.isAnnotationPresent(GetMapping.class)) {
            GetMapping annotation = method.getAnnotation(GetMapping.class);
            methodUrl = annotation.value();
            httpMethod = "GET";
        } else if (method.isAnnotationPresent(PostMapping.class)) {
            PostMapping annotation = method.getAnnotation(PostMapping.class);
            methodUrl = annotation.value();
            httpMethod = "POST";
        } else if (method.isAnnotationPresent(PutMapping.class)) {
            PutMapping annotation = method.getAnnotation(PutMapping.class);
            methodUrl = annotation.value();
            httpMethod = "PUT";
        } else if (method.isAnnotationPresent(DeleteMapping.class)) {
            DeleteMapping annotation = method.getAnnotation(DeleteMapping.class);
            methodUrl = annotation.value();
            httpMethod = "DELETE";
        } else if (method.isAnnotationPresent(RequestMapping.class)) {
            RequestMapping annotation = method.getAnnotation(RequestMapping.class);
            methodUrl = annotation.value();
            if (!annotation.method().isEmpty()) {
                httpMethod = annotation.method();
            }
        }

        // 组合完整的URL
        String fullUrl = combineUrl(baseUrl, methodUrl);

        // 如果没有指定HTTP方法，则注册所有支持的方法
        if (httpMethod.isEmpty()) {
            for (String supportedMethod : Arrays.asList("GET", "POST", "PUT", "DELETE")) {
                RequestMappingInfo mappingInfo = new RequestMappingInfo(fullUrl, supportedMethod);
                handlerMapping.put(mappingInfo, method);
            }
        } else {
            RequestMappingInfo mappingInfo = new RequestMappingInfo(fullUrl, httpMethod.toUpperCase());
            handlerMapping.put(mappingInfo, method);
        }

        // 注册方法与控制器的映射关系
        controllerMapping.put(method, controller);
    }


    /**
     * 合并 URL
     */
    private String combineUrl(String baseUrl, String methodUrl) {
        baseUrl = baseUrl.trim().replaceAll("^/+|/+$", "");
        methodUrl = methodUrl.trim().replaceAll("^/+|/+$", "");
        return "/" + baseUrl + (baseUrl.isEmpty() ? "" : "/") + methodUrl;
    }

    /**
     * 打印所有映射
     */
    private void printMappings() {
        System.out.println("\n=== Registered URL Mappings ===");
        handlerMapping.forEach((mapping, method) -> {
            System.out.println(String.format("%-6s %-30s -> %s.%s",
                    mapping.getMethod(),
                    mapping.getUrl(),
                    method.getDeclaringClass().getSimpleName(),
                    method.getName()));
        });
        System.out.println("=== Registered URL Mappings ===\n");
    }

    /**
     * 处理请求
     */
    private void handleRequest(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String uri = req.getRequestURI();
        String httpMethod = req.getMethod();

        Method handlerMethod = handlerMapping.get(new RequestMappingInfo(uri, httpMethod));

        // 405的情况
        if (handlerMethod == null) {
            for (Map.Entry<RequestMappingInfo, Method> entry : handlerMapping.entrySet()) {
                if (entry.getKey().getUrl().equals(uri)) {
                    resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                    resp.getWriter().write("Method Error: " + httpMethod + " not supported, try " + entry.getKey().getMethod() + " instead.");
                    return;
                }
            }
        }
        // 404的情况
        if (handlerMethod == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("404 Not Found: " + uri);
            return;
        }
        // 200正常请求
        invokeHandler(handlerMethod, req, resp);
    }

    /**
     * 调用处理器方法
     */
    private void invokeHandler(Method handlerMethod, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            Object controller = controllerMapping.get(handlerMethod);
            Parameter[] parameters = handlerMethod.getParameters();
            Object[] args = new Object[parameters.length];

            // 根据注解处理传参
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
                    try {
                        args[i] = applicationContext.getBean(parameter.getType());
                    } catch (Exception e) {
                        args[i] = null;
                    }
                }
            }

            // 检查响应类型注解
            boolean isClassResponseBody = controller.getClass().isAnnotationPresent(ResponseBody.class) ||
                    controller.getClass().isAnnotationPresent(RestController.class);
            boolean isMethodResponseBody = handlerMethod.isAnnotationPresent(ResponseBody.class);

            // 执行处理器方法
            Object result = handlerMethod.invoke(controller, args);

            // 处理响应，如果有@ResponseBody注解，返回JSON格式
            if (isClassResponseBody || isMethodResponseBody) {
                resp.setContentType("application/json;charset=utf-8");
                String json = objectMapper.writeValueAsString(result);
                resp.getWriter().print(json);
            } else {
                resp.getWriter().write(result != null ? result.toString() : "");
            }
        } catch (Exception e) {
            System.err.println("Error invoking handler method: " + e.getMessage());
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Internal Server Error: " + e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handleRequest(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handleRequest(req, resp);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handleRequest(req, resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        handleRequest(req, resp);
    }
}