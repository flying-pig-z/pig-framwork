package com.flyingpig.ioc.test.product;

import com.flyingpig.ioc.annotation.Component;

import java.util.UUID;

@Component
public class ProductDao {
    private final String id = UUID.randomUUID().toString();

    public String get() {
        return "Product found, dao instance: " + id;
    }
}
