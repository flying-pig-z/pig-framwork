package com.flyingpig.demo.controller;

import org.springframework.stereotype.Component;

@Component
public class TestService {

    public String test(){
        System.out.println("test...");
        return "Hello World!";
    }
}
