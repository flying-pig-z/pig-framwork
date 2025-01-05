package com.flyingpig.ioc.test.user;

import com.flyingpig.ioc.annotation.Autowired;
import com.flyingpig.ioc.annotation.Component;

@Component
public class UserService {
    private UserDao userDao;

    @Autowired
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public String saveUser() {
        return userDao.save();
    }
}
