package com.yan.examplespringbootprovider;

import com.yan.example.common.model.User;
import com.yan.example.common.service.UserService;
import com.yan.yanrpcspringbootstarter.annotation.RpcService;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现类
 *
 */
@Service
@RpcService
public class UserServiceImpl implements UserService {

    public User getUser(User user) {
        System.out.println("用户名：" + user.getName());
        throw new RuntimeException("测试异常");
//        return user;
    }
}
