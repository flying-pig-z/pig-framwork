package com.flyingpig.mvc.core;

import com.flyingpig.mvc.annotation.Controller;
import com.flyingpig.mvc.annotation.RestController;
import com.flyingpig.mvc.annotation.mapping.*;
import com.flyingpig.mvc.model.HandlerMethod;
import com.flyingpig.mvc.model.RequestMappingInfo;
import org.springframework.context.ApplicationContext;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * HandlerMapping 类用于管理请求 URL 和控制器方法之间的映射关系。
 * 主要功能是根据请求的 URL 和 HTTP 方法从控制器中找到相应的处理方法，负责初始化 URL 映射和处理请求的路由。
 */
public class HandlerMapping {

    // 存储所有请求 URL 和 HTTP 方法与对应的处理方法之间的映射
    private final Map<RequestMappingInfo, HandlerMethod> handlerMethods = new ConcurrentHashMap<>();

    // 用于获取 Spring 容器中注册的 Bean
    private ApplicationContext applicationContext;

    /**
     * 设置 ApplicationContext，供该类获取控制器 Bean。
     *
     * @param applicationContext 应用上下文，用于获取控制器 Bean
     */
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * 初始化所有控制器的方法映射。
     * 该方法通过扫描应用上下文中的所有控制器 Bean，解析其中的方法，并将 URL 映射和方法信息注册到 handlerMethods 中。
     */
    public void initMapping() {
        // 获取所有带有 @Controller 注解的 Bean
        Map<String, Object> controllers = applicationContext.getBeansWithAnnotation(Controller.class);
        // 注册 URL + 请求方法与实际方法的映射
        controllers.forEach((name, controller) -> registerController(controller));
        // 打印所有注册的 URL 映射
        printMappings();
    }

    /**
     * 根据请求的 URI 和 HTTP 方法获取对应的 HandlerMethod。
     *
     * @param request HTTP 请求
     * @return 处理该请求的 HandlerMethod，如果没有找到匹配的处理方法，则返回 null
     */
    public HandlerMethod getHandler(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String httpMethod = request.getMethod();

        // 遍历所有的映射，找到匹配的处理器
        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
            RequestMappingInfo mappingInfo = entry.getKey();

            // 1. 检查HTTP方法是否匹配
            if (!mappingInfo.getMethod().equals(httpMethod)) {
                continue;
            }

            // 2. 检查URL是否匹配（支持路径变量）
            if (matchUrl(mappingInfo.getUrl(), uri)) {
                return entry.getValue();
            }
        }

        return null;
    }

    /**
     * 检查URL是否匹配，支持路径变量
     */
    private boolean matchUrl(String pattern, String actualUrl) {
        // 移除首尾的斜杠
        pattern = pattern.replaceAll("^/+|/+$", "");
        actualUrl = actualUrl.replaceAll("^/+|/+$", "");

        String[] patternParts = pattern.split("/");
        String[] actualParts = actualUrl.split("/");

        // 部分数量必须相同
        if (patternParts.length != actualParts.length) {
            return false;
        }

        // 逐部分比较
        for (int i = 0; i < patternParts.length; i++) {
            String patternPart = patternParts[i];
            String actualPart = actualParts[i];

            // 如果是路径变量（{xxx}格式），则认为匹配
            if (patternPart.startsWith("{") && patternPart.endsWith("}")) {
                continue;
            }

            // 否则必须精确匹配
            if (!patternPart.equals(actualPart)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 注册 URL + 请求方法与实际方法的映射
     *
     * @param controller 控制器 Bean
     */
    private void registerController(Object controller) {
        // 获取控制器类的类型
        Class<?> controllerClass = controller.getClass();
        // 获取控制器类的基础 URL（如果有的话）
        String baseUrl = getBaseUrl(controllerClass);

        // 遍历控制器类中的所有方法，处理每个方法的映射
        for (Method method : controllerClass.getMethods()) {
            processMethod(method, controller, baseUrl);
        }
    }

    /**
     * 获取控制器类的基础 URL（如果控制器类上有 @RequestMapping 注解）。
     *
     * @param controllerClass 控制器类
     * @return 控制器的基础 URL
     */
    private String getBaseUrl(Class<?> controllerClass) {
        if (controllerClass.isAnnotationPresent(RequestMapping.class)) {
            // 获取 @RequestMapping 注解的 value 值
            return controllerClass.getAnnotation(RequestMapping.class).value();
        }
        return "";
    }

    /**
     * 处理控制器方法的 URL 映射。
     *
     * @param method     控制器方法
     * @param controller 控制器对象
     * @param baseUrl    控制器的基础 URL
     */
    private void processMethod(Method method, Object controller, String baseUrl) {
        // 默认方法 URL 和 HTTP 方法
        String methodUrl;
        String httpMethod;

        // 根据方法上的注解设置 URL 和 HTTP 方法
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
            httpMethod = annotation.method();
        } else {
            return;
        }

        // 拼接控制器基础 URL 和方法 URL，形成完整的 URL
        String fullUrl = combineUrl(baseUrl, methodUrl);

        // 如果 HTTP 方法为空，则表示该方法支持多种 HTTP 方法（如 GET、POST、PUT、DELETE）
        if (httpMethod.isEmpty()) {
            for (String supportedMethod : Arrays.asList("GET", "POST", "PUT", "DELETE")) {
                RequestMappingInfo mappingInfo = new RequestMappingInfo(fullUrl, supportedMethod);
                handlerMethods.put(mappingInfo, new HandlerMethod(controller, method, mappingInfo));
            }
        } else {
            // 否则只注册指定的 HTTP 方法
            RequestMappingInfo mappingInfo = new RequestMappingInfo(fullUrl, httpMethod.toUpperCase());
            handlerMethods.put(mappingInfo, new HandlerMethod(controller, method, mappingInfo));
        }
    }

    /**
     * 拼接控制器的基础 URL 和方法的 URL，形成完整的 URL。
     *
     * @param baseUrl   控制器的基础 URL
     * @param methodUrl 方法的 URL
     * @return 拼接后的完整 URL
     */
    private String combineUrl(String baseUrl, String methodUrl) {
        baseUrl = baseUrl.trim().replaceAll("^/+|/+$", "");
        methodUrl = methodUrl.trim().replaceAll("^/+|/+$", "");

        // 如果methodUrl为空，不要加后面的斜杠
        if (methodUrl.isEmpty()) {
            return "/" + baseUrl;
        }

        return "/" + baseUrl + "/" + methodUrl;
    }

    /**
     * 打印所有注册的 URL 映射信息，方便调试。
     */
    private void printMappings() {
        System.out.println("\n=== Registered URL Mappings ===");
        handlerMethods.forEach((mapping, method) -> {
            System.out.println(String.format("%-6s %-30s -> %s.%s",
                    mapping.getMethod(),
                    mapping.getUrl(),
                    method.getMethod().getDeclaringClass().getSimpleName(),
                    method.getMethod().getName()));
        });
        System.out.println("=== End of URL Mappings ===\n");
    }
}
