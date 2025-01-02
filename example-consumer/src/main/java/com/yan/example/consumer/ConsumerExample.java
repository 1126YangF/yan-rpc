package com.yan.example.consumer;

import com.yan.example.common.model.User;
import com.yan.example.common.service.UserService;
import com.yan.rpc.bootstrap.ConsumerBootstrap;
import com.yan.rpc.proxy.ServiceProxyFactory;

/**
 * 简易服务消费者示例
 */
public class ConsumerExample {

    public static void main(String[] args) throws InterruptedException {
        // 服务消费者初始化
        ConsumerBootstrap.init();
        // 获取代理
        UserService userService = ServiceProxyFactory.getProxy(UserService.class);
        User user = new User();
        user.setName("yupi");
        // 调用
        User newUser = userService.getUser(user);

        if (newUser != null) {
            System.out.println("username======" + newUser.getName());
        } else {
            System.out.println("user == null");
        }
        long number = userService.getNumber();
        System.out.println(number);
    }
}