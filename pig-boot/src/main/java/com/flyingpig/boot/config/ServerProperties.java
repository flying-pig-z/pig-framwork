package com.flyingpig.boot.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ServerProperties {

    @Value("${server.port:8080}") // 默认值为 8080
    private int port;

    @Value("${server.type:tomcat}") // 默认值为 tomcat,可以选择jetty
    private String type;

    public int getPort() { return port; }
    public String getType() { return type; }
}
