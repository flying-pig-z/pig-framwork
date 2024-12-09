package com.flyingpig.mvc.model;

import java.lang.reflect.Method;


/**
 * HandlerMethod： 处理器方法封装类 - 封装Controller方法相关信息.
 * 该类封装了请求处理方法的相关信息，包括控制器实例、方法对象以及该方法的请求映射信息。
 */
public class HandlerMethod {

    // 控制器实例，表示该方法所属的控制器对象
    private final Object controller;

    // 处理请求的具体方法
    private final Method method;

    // 请求映射信息，包含该方法的 URL 映射和 HTTP 方法等信息
    private final RequestMappingInfo mappingInfo;

    /**
     * 构造函数，初始化 HandlerMethod。
     *
     * @param controller 控制器实例，表示该方法所属的控制器对象
     * @param method 处理请求的具体方法
     * @param mappingInfo 请求映射信息，描述该方法的请求 URL 和 HTTP 方法等
     */
    public HandlerMethod(Object controller, Method method, RequestMappingInfo mappingInfo) {
        this.controller = controller;
        this.method = method;
        this.mappingInfo = mappingInfo;
    }

    /**
     * 获取该方法对应的控制器对象。
     *
     * @return 控制器实例
     */
    public Object getController() {
        return controller;
    }

    /**
     * 获取该方法对象。
     *
     * @return 处理请求的 Method 对象
     */
    public Method getMethod() {
        return method;
    }

    /**
     * 获取该方法的请求映射信息。
     *
     * @return 请求映射信息，包含 URL 映射、HTTP 方法等信息
     */
    public RequestMappingInfo getMappingInfo() {
        return mappingInfo;
    }
}
