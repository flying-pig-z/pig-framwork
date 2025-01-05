package com.flyingpig.ioc.test;

import com.flyingpig.ioc.core.BeanDefinition;
import com.flyingpig.ioc.core.BeanScope;
import com.flyingpig.ioc.core.SimpleIoC;
import com.flyingpig.ioc.test.database.ConfigService;
import com.flyingpig.ioc.test.database.ConnectionManager;
import com.flyingpig.ioc.test.database.DatabaseConfig;
import com.flyingpig.ioc.test.order.OrderDao;
import com.flyingpig.ioc.test.order.OrderService;
import com.flyingpig.ioc.test.product.ProductDao;
import com.flyingpig.ioc.test.product.ProductService;
import com.flyingpig.ioc.test.user.UserDao;
import com.flyingpig.ioc.test.user.UserService;

// 完整测试用例
public class IoCTest {
    public static void main(String[] args) throws Exception {
        SimpleIoC container = new SimpleIoC();

        // 测试@Component注册和构造器注入
        // OrderService依赖于OrderDao
        // 如果一个类只有一个构造函数,那么这个构造函数的参数会被自动注入,不需要 @Autowired，这里模仿这个特性
        container.registerComponent(OrderDao.class);
        container.registerComponent(OrderService.class);
        OrderService orderService = (OrderService) container.getBean("orderService");
        System.out.println("构造器注入测试结果: " + orderService.createOrder());

        // 测试setter注入
        container.registerComponent(UserDao.class);
        container.registerComponent(UserService.class);
        UserService userService = (UserService) container.getBean("userService");
        System.out.println("Setter注入测试结果: " + userService.saveUser());

        // 测试字段注入
        container.registerComponent(ProductDao.class);
        container.registerComponent(ProductService.class);
        ProductService productService = (ProductService) container.getBean("productService");
        System.out.println("字段注入测试结果: " + productService.getProduct());

        // 测试prototype作用域，利用修改ProductDao为prototype作用域
        BeanDefinition productDaoDefinition = container.getBeanDefinition("productDao");
        productDaoDefinition.setScope(BeanScope.PROTOTYPE);
        ProductService productService1 = (ProductService) container.getBean("productService");
        ProductService productService2 = (ProductService) container.getBean("productService");
        System.out.println("原型作用域测试结果:");
        System.out.println("   第一次调用: " + productService1.getProduct());
        System.out.println("   第二次调用: " + productService2.getProduct());

        // 测试@Bean注册和带参数构造函数
        container.registerBean("configService", ConfigService.class);
        ConfigService configService = new ConfigService("/etc/app/config", true);
        System.out.println("带参数构造函数测试结果: " + configService.getConfig());

        // 测试@Value注入属性和Aware接口有没有成功注入容器内容
        container.registerComponent(DatabaseConfig.class);
        DatabaseConfig dbConfig = (DatabaseConfig) container.getBean("databaseConfig");
        System.out.println("属性注入和Aware接口测试结果:");
        System.out.println("   " + dbConfig.getConfigInfo());
        System.out.println("   BeanFactory感知测试: " + (dbConfig.getBeanFactory() == container ? "通过" : "失败"));

        // 测试@PreDestroy初始化方法注解
        System.out.println("\n生命周期方法测试结果:");
        container.registerComponent(ConnectionManager.class);
        ConnectionManager connManager = (ConnectionManager) container.getBean("connectionManager");
        System.out.println("   初始化测试: " +
                (connManager.isInitialized() ? "通过" : "失败"));
        System.out.println("   连接测试: " +
                (connManager.isConnected() ? "通过" : "失败"));
        // 手动触发容器关闭以测试@PreDestroy
        System.out.println("\n正在关闭容器...");
        container.close();

        System.out.println("\n所有测试已成功完成！");
    }
}
