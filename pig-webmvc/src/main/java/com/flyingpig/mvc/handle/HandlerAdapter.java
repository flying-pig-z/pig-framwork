package com.flyingpig.mvc.handle;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.flyingpig.mvc.annotation.RequestBody;
import com.flyingpig.mvc.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public class HandlerAdapter {

    private ObjectMapper objectMapper = new ObjectMapper();

    public boolean supports(Object handler) {
        return (handler instanceof HandlerMapping);
    }

    public void handle(HttpServletRequest req, HttpServletResponse resp, Object handler) throws Exception {
        HandlerMapping handlerMapping = (HandlerMapping) handler;
        Method method = handlerMapping.getMethod();
        Object[] args = getArgs(req, method);

        Object controller = handlerMapping.getController();
        Object result = method.invoke(controller, args);

        if (method.isAnnotationPresent(ResponseBody.class)) {
            handleResponseBody(resp, result);
        } else {
            // 处理视图
            handleViewResult(resp, result);
        }
    }

    private Object[] getArgs(HttpServletRequest req, Method method) throws IOException {
        Parameter[] params = method.getParameters();
        Object[] args = new Object[params.length];
        for (int i = 0; i < params.length; i++) {
            Parameter param = params[i];
            if (param.isAnnotationPresent(RequestBody.class)) {
                BufferedReader reader = req.getReader();
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                args[i] = sb.toString();
            } else {
                String value = (String) req.getAttribute(param.getName());
                args[i] = value;
            }
        }
        return args;
    }

    private void handleResponseBody(HttpServletResponse resp, Object result) throws IOException {
        resp.setContentType("application/json;charset=utf-8");
        String json = objectMapper.writeValueAsString(result);
        resp.getWriter().print(json);
    }

    private void handleViewResult(HttpServletResponse resp, Object result) throws Exception {
        if (result instanceof String) {
            String viewName = (String) result;
            resp.sendRedirect(viewName);
        }
    }
}