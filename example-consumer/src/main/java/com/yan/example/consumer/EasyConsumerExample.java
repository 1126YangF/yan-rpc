package com.yan.example.consumer;

import com.yan.rpc.config.RpcConfig;
import com.yan.rpc.utils.ConfigUtils;

/**
 * @Author: Yang_f
 * @CreateTime: 2024-12-04 14:38
 * @Description:
 */

public class EasyConsumerExample {

    public static void main(String[] args) {
//        UserService userService = ServiceProxyFactory.getProxy(UserService.class);
//        User user = new User();
//        user.setName("test");
//        //调用
//        User user1 = userService.getUser(user);
//        if (user1 != null) {
//            System.out.println(user1.getName());
//        } else {
//            System.out.println("user == null");
//        }

        RpcConfig rpc = ConfigUtils.loadConfig(RpcConfig.class, "rpc");
        System.out.println(rpc);
    }
}
