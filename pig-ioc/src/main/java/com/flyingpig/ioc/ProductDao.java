package com.flyingpig.ioc;

import java.util.UUID;

@Component
class ProductDao {
    private final String id = UUID.randomUUID().toString();

    public String get() {
        return "Product found, dao instance: " + id;
    }
}
