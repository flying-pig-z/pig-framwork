package com.flyingpig.ioc;

// 测试字段注入和prototype作用域
@Component
class ProductService {
    @Autowired
    private ProductDao productDao;

    public String getProduct() {
        return productDao.get();
    }
}
