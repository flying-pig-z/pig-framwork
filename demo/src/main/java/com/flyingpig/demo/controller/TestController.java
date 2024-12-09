package com.flyingpig.demo.controller;

import com.flyingpig.mvc.annotation.*;
import com.flyingpig.mvc.annotation.mapping.GetMapping;
import com.flyingpig.mvc.annotation.mapping.RequestMapping;
import com.flyingpig.mvc.annotation.request.PathVariable;
import com.flyingpig.mvc.annotation.request.RequestBody;
import com.flyingpig.mvc.annotation.request.RequestParam;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping(value = "/test")
public class TestController {

    @Autowired
    TestService testService;


    @GetMapping("/hello-world")
    public Result hello() {
        return new Result(200, testService.test());
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
