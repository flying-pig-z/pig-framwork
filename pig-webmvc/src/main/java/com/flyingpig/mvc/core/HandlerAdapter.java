package com.flyingpig.mvc.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flyingpig.mvc.annotation.request.RequestParam;
import com.flyingpig.mvc.annotation.response.ResponseBody;
import com.flyingpig.mvc.annotation.RestController;
import com.flyingpig.mvc.model.HandlerMethod;
import org.springframework.context.ApplicationContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;


/**
 * HandlerAdapter 类用于处理 HTTP 请求，执行控制器方法，并根据需要将结果返回给客户端。
 */
public class HandlerAdapter {

    // ObjectMapper 用于将结果转换为 JSON 格式
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ApplicationContext 用于获取依赖注入的 bean
    private ApplicationContext applicationContext;

    /**
     * 设置 ApplicationContext
     * @param applicationContext 应用上下文，用于获取注入的 bean
     */
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * 处理 HTTP 请求，执行相应的控制器方法并将结果返回给响应。
     * @param request HTTP 请求
     * @param response HTTP 响应
     * @param handler 控制器方法
     * @throws Exception 如果方法执行过程中发生错误，则抛出异常
     */
    public void handle(HttpServletRequest request, HttpServletResponse response, HandlerMethod handler) throws Exception {
        // 进行前置处理 -- 根据注解对参数进行处理
        Object[] args = preHandle(request, response, handler.getMethod());

        // 执行控制器方法，获取结果
        Object result = handler.getMethod().invoke(handler.getController(), args);

        // 进行后置处理 -- 看是否需要序列化，并将结果返回给客户端
        postHandle(response, result, handler);
    }

    /**
     * 处理前置逻辑，获取控制器方法的参数并填充。
     * @param request HTTP 请求
     * @param response HTTP 响应
     * @param method 控制器方法
     * @return 参数数组，用于方法调用
     */
    private Object[] preHandle(HttpServletRequest request, HttpServletResponse response, Method method) {
        // 获取方法的参数列表
        Parameter[] parameters = method.getParameters();
        // 用于存储方法的参数
        Object[] args = new Object[parameters.length];

        // 遍历每个参数，进行填充
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];

            // 如果参数类型是 HttpServletRequest，则传入请求对象
            if (parameter.getType().equals(HttpServletRequest.class)) {
                args[i] = request;
            }
            // 如果参数类型是 HttpServletResponse，则传入响应对象
            else if (parameter.getType().equals(HttpServletResponse.class)) {
                args[i] = response;
            }
            // 如果参数有 @RequestParam 注解，则从请求参数中获取对应值
            else if (parameter.isAnnotationPresent(RequestParam.class)) {
                String paramName = parameter.getAnnotation(RequestParam.class).value();
                args[i] = request.getParameter(paramName);
            }
            // 如果参数没有特别注解，则从 ApplicationContext 获取对应的 Bean
            else {
                try {
                    args[i] = applicationContext.getBean(parameter.getType());
                } catch (Exception e) {
                    // 如果没有找到 Bean，则设置为 null
                    args[i] = null;
                }
            }
        }
        return args;
    }

    /**
     * 处理后置逻辑，将方法执行结果写入响应。
     * @param response HTTP 响应
     * @param result 方法执行结果
     * @param handler 控制器方法
     * @throws IOException 如果写入响应时发生错误
     */
    private void postHandle(HttpServletResponse response, Object result, HandlerMethod handler) throws IOException {
        // 检查控制器或方法是否有 @ResponseBody 或 @RestController 注解，决定是否以 JSON 格式返回
        boolean isResponseBody = handler.getController().getClass().isAnnotationPresent(ResponseBody.class) ||
                handler.getController().getClass().isAnnotationPresent(RestController.class) ||
                handler.getMethod().isAnnotationPresent(ResponseBody.class);

        // 如果是 @ResponseBody 或 @RestController，则返回 JSON 格式的响应
        if (isResponseBody) {
            response.setContentType("application/json;charset=utf-8");
            String json = objectMapper.writeValueAsString(result);
            response.getWriter().print(json);
        } else {
            // 否则返回方法的字符串表示
            response.getWriter().write(result != null ? result.toString() : "");
        }
    }
}
