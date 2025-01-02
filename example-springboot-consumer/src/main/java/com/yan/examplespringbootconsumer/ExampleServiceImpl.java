package com.yan.examplespringbootconsumer;

import com.yan.example.common.model.User;
import com.yan.example.common.service.UserService;
import com.yan.rpc.fault.retry.RetryStrategyKeys;
import com.yan.rpc.fault.tolerant.TolerantStrategyKeys;
import com.yan.yanrpcspringbootstarter.annotation.RpcReference;
import org.springframework.stereotype.Service;

@Service
public class ExampleServiceImpl {

    @RpcReference(retryStrategy = RetryStrategyKeys.FIXED_INTERVAL, tolerantStrategy = TolerantStrategyKeys.FAIL_SAFE)
    private UserService userService;

    @RpcReference(retryStrategy = RetryStrategyKeys.FIXED_INTERVAL, tolerantStrategy = TolerantStrategyKeys.FAIL_SAFE)
    private UserService userService2;

    public void test() {
        User user = new User();
        user.setName("yupi");
        User resultUser = userService.getUser(user);
        System.out.println(resultUser);
    }

}
