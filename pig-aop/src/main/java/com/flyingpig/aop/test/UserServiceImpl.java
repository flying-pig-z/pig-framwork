package com.flyingpig.aop.test;

public class UserServiceImpl implements UserService {

    @Log
    @Override
    public void getUserId(String userId) {
        System.out.println("Executing getUserInfo for: " + userId);
    }
}
