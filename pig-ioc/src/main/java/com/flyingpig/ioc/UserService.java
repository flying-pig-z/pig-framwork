package com.flyingpig.ioc;

// 测试setter注入
@Component
class UserService {
    private UserDao userDao;

    @Autowired
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public String saveUser() {
        return userDao.save();
    }
}
