package com.flyingpig.ioc.test.user;

import com.flyingpig.ioc.annotation.Component;

@Component
public class UserDao {
    public String save() {
        return "User saved";
    }
}
