package com.flyingpig.ioc.test.product;

import com.flyingpig.ioc.annotation.Autowired;
import com.flyingpig.ioc.annotation.Component;

// 测试字段注入和prototype作用域
@Component
public class ProductService {

    @Autowired
    private ProductDao productDao;

    public String getProduct() {
        return productDao.get();
    }
}
