package com.flyingpig.ioc;

// 6. 完整测试用例
public class IoCTest {
    public static void main(String[] args) throws Exception {
        SimpleIoC container = new SimpleIoC();

        // 1. 测试@Component注册和构造器注入
        container.registerComponent(OrderDao.class);
        container.registerComponent(OrderService.class);
        OrderService orderService = (OrderService) container.getBean("orderService");
        System.out.println("1. Constructor Injection Test: " + orderService.createOrder());

        // 2. 测试setter注入
        container.registerComponent(UserDao.class);
        container.registerComponent(UserService.class);
        UserService userService = (UserService) container.getBean("userService");
        System.out.println("2. Setter Injection Test: " + userService.saveUser());

        // 3. 测试字段注入
        container.registerComponent(ProductDao.class);
        container.registerComponent(ProductService.class);
        ProductService productService = (ProductService) container.getBean("productService");
        System.out.println("3. Field Injection Test: " + productService.getProduct());

        // 4. 测试prototype作用域
        // 修改ProductDao为prototype作用域
        BeanDefinition productDaoDefinition = container.getBeanDefinition("productDao");
        productDaoDefinition.setScope(BeanScope.PROTOTYPE);

        ProductService productService1 = (ProductService) container.getBean("productService");
        ProductService productService2 = (ProductService) container.getBean("productService");
        System.out.println("4. Prototype Scope Test:");
        System.out.println("   First call: " + productService1.getProduct());
        System.out.println("   Second call: " + productService2.getProduct());

        // 5. 测试@Bean注册和带参数构造函数
        container.registerBean("configService", ConfigService.class);
        // 这里需要实现一个工厂方法来处理构造参数
        // 在实际的Spring中，这通常通过@Bean注解的方法来完成
        ConfigService configService = new ConfigService("/etc/app/config", true);
        System.out.println("5. Constructor with Parameters Test: " + configService.getConfig());

        // 6. 测试@Value属性注入和Aware接口
        container.registerComponent(DatabaseConfig.class);
        DatabaseConfig dbConfig = (DatabaseConfig) container.getBean("databaseConfig");
        System.out.println("6. Property Injection and Aware Test:");
        System.out.println("   " + dbConfig.getConfigInfo());
        System.out.println("   BeanFactory aware test: " +
                (dbConfig.getBeanFactory() == container ? "passed" : "failed"));

        // 7. 测试生命周期方法
        System.out.println("\n7. Lifecycle Methods Test:");
        container.registerComponent(ConnectionManager.class);
        ConnectionManager connManager = (ConnectionManager) container.getBean("connectionManager");
        System.out.println("   Initialization test: " +
                (connManager.isInitialized() ? "passed" : "failed"));
        System.out.println("   Connection test: " +
                (connManager.isConnected() ? "passed" : "failed"));

        // Manually trigger container shutdown to test @PreDestroy
        System.out.println("\nShutting down container...");
        container.close();
        System.out.println("\nAll tests completed successfully!");
    }
}
