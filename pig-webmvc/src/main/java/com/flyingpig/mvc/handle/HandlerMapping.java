package com.flyingpig.mvc.handle;


import com.flyingpig.mvc.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class HandlerMapping {

    private Object controller;

    private Method method;

    private Pattern pattern;

    private Map<String, String> paramIndexMapping;

    public HandlerMapping(Object controller, Method method, String url) {
        this.controller = controller;
        this.method = method;
        this.paramIndexMapping = new HashMap<>();

        url = url.replaceAll("\\*", ".*");

        String paramPattern = "\\{[^/]+?\\}";
        Pattern pattern = Pattern.compile(paramPattern);
        java.util.regex.Matcher matcher = pattern.matcher(url);

        StringBuilder sb = new StringBuilder("^");
        int i = 0;
        while (matcher.find()) {
            String str = matcher.group();
            String paramName = str.substring(1, str.length() - 1);
            paramIndexMapping.put(paramName, String.valueOf(i));
            matcher.appendReplacement(sb, "([^/]+?)");
            i++;
        }
        matcher.appendTail(sb);
        sb.append("$");

        this.pattern = Pattern.compile(sb.toString());
    }

    public Object getController() {
        return controller;
    }

    public void setController(Object controller) {
        this.controller = controller;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public boolean match(String url) {
        return pattern.matcher(url).matches();
    }

    public Map<String, String> getParamIndexMapping() {
        return paramIndexMapping;
    }

    public void parseParams(HttpServletRequest req) throws Exception {
        Map<String, String> params = getUrlParams(req);

        Class<?>[] paramTypes = method.getParameterTypes();
        for (Map.Entry<String, String> param : params.entrySet()) {
            String paramName = param.getKey();
            String paramValue = param.getValue();
            if (paramIndexMapping.containsKey(paramName)) {
                int index = Integer.parseInt(paramIndexMapping.get(paramName));
                Class<?> paramType = paramTypes[index];
                Object value;
                if (paramType == String.class) {
                    value = paramValue;
                } else {
                    value = parseNumber(paramType, paramValue);
                }
                req.setAttribute(paramName, value);
            }
        }

        Annotation[][] pas = method.getParameterAnnotations();
        for (int i = 0; i < paramTypes.length; i++) {
            Class<?> paramType = paramTypes[i];
            Annotation[] pas2 = pas[i];
            for (Annotation a : pas2) {
                if (a instanceof RequestParam) {
                    RequestParam rp = (RequestParam) a;
                    String value = req.getParameter(rp.value());
                    req.setAttribute(rp.value(), value);
                }
            }
        }
    }

    private Map<String, String> getUrlParams(HttpServletRequest req) {
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replace(contextPath, "").replaceAll("/+", "/");

        java.util.regex.Matcher matcher = pattern.matcher(url);
        Map<String, String> params = new HashMap<>();

        if (matcher.find()) {
            for (Map.Entry<String, String> entry : paramIndexMapping.entrySet()) {
                String key = entry.getKey();
                String value = matcher.group(Integer.parseInt(entry.getValue()) + 1);
                params.put(key, value);
            }
        }

        return params;
    }

    private Object parseNumber(Class<?> paramType, String paramValue) {
        if (paramType == Integer.class) {
            return Integer.valueOf(paramValue);
        } else if (paramType == Double.class) {
            return Double.valueOf(paramValue);
        } else if (paramType == Long.class) {
            return Long.valueOf(paramValue);
        } else if (paramType == Float.class) {
            return Float.valueOf(paramValue);
        }
        return paramValue;
    }
}