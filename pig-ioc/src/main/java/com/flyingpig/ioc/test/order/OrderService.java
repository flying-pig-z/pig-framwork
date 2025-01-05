package com.flyingpig.ioc.test.order;

import com.flyingpig.ioc.annotation.Autowired;
import com.flyingpig.ioc.annotation.Component;


@Component
public class OrderService {
    private final OrderDao orderDao;

    @Autowired
    public OrderService(OrderDao orderDao) {
        this.orderDao = orderDao;
    }

    public String createOrder() {
        return orderDao.create();
    }
}
