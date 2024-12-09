package com.flyingpig.mvc.core;

import com.flyingpig.mvc.model.HandlerMethod;
import org.springframework.context.ApplicationContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * DispatcherServlet：请求分发处理器
 * 负责处理所有的 HTTP 请求的分发和处理
 */
public class DispatcherServlet extends HttpServlet {

    // HandlerMapping 用于根据请求的 URL 和 HTTP 方法查找匹配的 HandlerMethod
    private final HandlerMapping handlerMapping;

    // HandlerAdapter 用于执行 handler 方法，并进行请求的前后处理
    private final HandlerAdapter handlerAdapter;

    // Spring 容器，用于获取 Bean 和初始化各种组件
    private ApplicationContext applicationContext;

    /**
     * 构造函数，初始化 DispatcherServlet。
     * @param applicationContext Spring 容器，用于获取和管理所有的 Bean
     */
    public DispatcherServlet(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;

        // 初始化 HandlerMapping 和 HandlerAdapter
        this.handlerMapping = new HandlerMapping();
        this.handlerMapping.setApplicationContext(applicationContext);

        this.handlerAdapter = new HandlerAdapter();
        this.handlerAdapter.setApplicationContext(applicationContext);
    }

    /**
     * 初始化方法，Servlet 启动时调用。
     * 用于初始化 HandlerMapping 和相关的映射配置。
     * 如果没有设置 applicationContext 或初始化失败，将抛出异常。
     */
    @Override
    public void init() throws ServletException {
        // 检查 ApplicationContext 是否已经设置
        if (applicationContext == null) {
            throw new ServletException("ApplicationContext must be set before initialization");
        }

        try {
            // 初始化 HandlerMapping，加载控制器和映射
            handlerMapping.initMapping();
        } catch (Exception e) {
            // 初始化失败时抛出 ServletException
            throw new ServletException("Failed to initialize DispatcherServlet", e);
        }
    }

    /**
     * 处理 HTTP 请求。
     * 这个方法是 DispatcherServlet 处理请求的核心逻辑，负责获取匹配的处理方法并执行。
     *
     * 它首先通过 HandlerMapping 获取对应的 HandlerMethod，然后使用 HandlerAdapter 执行该方法。
     * 如果找不到匹配的处理方法，返回 404 错误。如果处理过程中发生异常，返回 500 错误。
     *
     * @param req HTTP 请求
     * @param resp HTTP 响应
     * @throws IOException IO 异常
     */
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            // 根据请求获取匹配的 HandlerMethod
            HandlerMethod handler = handlerMapping.getHandler(req);

            // 如果没有找到匹配的 HandlerMethod，则返回 404 Not Found 错误
            if (handler == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                resp.getWriter().write("404 Not Found: " + req.getRequestURI());
                return;
            }

            // 使用 HandlerAdapter 执行处理方法，并将结果写入响应
            handlerAdapter.handle(req, resp, handler);
        } catch (Exception e) {
            // 如果处理过程中发生异常，返回 500 Internal Server Error 错误
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Internal Server Error: " + e.getMessage());
        }
    }
}


