package com.flyingpig.demo.controller;

import com.flyingpig.mvc.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    Test test;

    @Autowired
    Testtoo testtoo;

    @RequestMapping("/hello-world")
    public Result hello() {
        test.test();
        testtoo.test();
        return new Result(200,"test", testtoo);
    }

    @RequestMapping("/hello/{name}")
    public String helloName(@PathVariable("name") String name) {
        return "Hello " + name + "!";
    }

    @RequestMapping("/hello/request")
    public String request(@RequestParam("param") String param, @RequestBody String body) {
        return "param: " + param + ", body: " + body;
    }
}
