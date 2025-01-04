package com.flyingpig.demo.controller;

import com.flyingpig.demo.common.Result;
import com.flyingpig.demo.entity.User;
import com.flyingpig.demo.service.UserService;
import com.flyingpig.mvc.annotation.*;
import com.flyingpig.mvc.annotation.mapping.*;
import com.flyingpig.mvc.annotation.request.PathVariable;
import com.flyingpig.mvc.annotation.request.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import com.flyingpig.mvc.annotation.request.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/user")
public class UserController {

    @Autowired
    UserService userService;

    // 获取用户
    @GetMapping("/{id}")
    public Result getUser(@PathVariable("id") String userId) {
        System.out.println("查询用户：" + userId);
        User selectUser = userService.getUserById(Long.parseLong(userId));
        if (selectUser == null) {
            return Result.error("用户不存在");
        }
        return Result.success(selectUser);
    }


    // 修改用户名称
    @PutMapping("/name")
    public Result modifyUserName(@RequestParam("id") String id,
                                 @RequestParam("name") String name) {
        System.out.println("修改用户 " + id + " 名称为:" + name);
        userService.updateUsername(id, name);
        return Result.success();
    }

    // 增加用户
    @PostMapping
    public Result addUser(@RequestBody User user) {
        System.out.println("添加用户：" + user);
        userService.createUser(user);
        return Result.success();
    }

    // 批量增加用户
    @PostMapping("/batch")
    public Result batchCreateUsers(@RequestBody List<User> users) {
        System.out.println("批量添加用户：");
        for (User user : users) {
            System.out.println(user);
        }
        userService.batchCreateUsers(users);
        return Result.success();
    }


    // 删除用户
    @DeleteMapping("/{id}")
    public Result deleteUser(@PathVariable("id") String id) {
        System.out.println("删除用户：" + id);
        userService.deleteUser(id);
        return Result.success();
    }
}
