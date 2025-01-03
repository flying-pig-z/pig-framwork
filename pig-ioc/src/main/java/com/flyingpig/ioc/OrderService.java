package com.flyingpig.ioc;

// 测试构造器注入
@Component
class OrderService {
    private final OrderDao orderDao;

    @Autowired
    public OrderService(OrderDao orderDao) {
        this.orderDao = orderDao;
    }

    public String createOrder() {
        return orderDao.create();
    }
}
