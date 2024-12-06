package com.flyingpig.boot.server;

import com.flyingpig.boot.config.ServerProperties;
import com.flyingpig.mvc.core.DispatcherServlet;

public class WebServerFactory {
    private static final WebServerFactory instance = new WebServerFactory();

    private WebServerFactory() {}

    public static WebServerFactory getInstance() {
        return instance;
    }

    public WebServer createWebServer(ServerProperties properties, DispatcherServlet dispatcherServlet) {
        return switch (properties.getType().toLowerCase()) {
            case "tomcat" -> createTomcatWebServer(properties.getPort(), dispatcherServlet);
            case "jetty" -> createJettyWebServer(properties.getPort(), dispatcherServlet);
            default -> throw new IllegalArgumentException("Unsupported server type: " + properties.getType());
        };
    }

    private WebServer createTomcatWebServer(int port, DispatcherServlet dispatcherServlet) {
        return new TomcatWebServer(port, dispatcherServlet);
    }

    private WebServer createJettyWebServer(int port, DispatcherServlet dispatcherServlet) {
        return new JettyWebServer(port, dispatcherServlet);
    }
}