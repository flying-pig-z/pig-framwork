package com.flyingpig.mvc.core;

import java.util.Objects;

/**
 * 请求映射信息
 * 用于存储 URL 和 HTTP 方法的组合信息，作为处理器方法的唯一标识
 */
public class RequestMappingInfo {
    private final String url;
    private final String method;

    /**
     * 构造函数
     *
     * @param url    请求URL
     * @param method HTTP方法
     */
    public RequestMappingInfo(String url, String method) {
        this.url = url;
        this.method = method;
    }

    /**
     * 获取URL
     *
     * @return 请求URL
     */
    public String getUrl() {
        return url;
    }

    /**
     * 获取HTTP方法
     *
     * @return HTTP方法
     */
    public String getMethod() {
        return method;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RequestMappingInfo that = (RequestMappingInfo) o;
        return Objects.equals(url, that.url) && Objects.equals(method, that.method);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, method);
    }

    @Override
    public String toString() {
        return "RequestMappingInfo{" +
                "url='" + url + '\'' +
                ", method='" + method + '\'' +
                '}';
    }
}

