package com.flyingpig.ioc.test.order;


import com.flyingpig.ioc.annotation.Component;

@Component
public class OrderDao {
    public String create() {
        return "Order created";
    }
}
